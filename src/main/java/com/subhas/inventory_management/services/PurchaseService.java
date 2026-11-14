package com.subhas.inventory_management.services;

import com.subhas.inventory_management.entity.*;
import com.subhas.inventory_management.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final BatchRepository batchRepository;
    private final StockLedgerRepository ledgerRepository;
    private final VendorRepository vendorRepository;
    private final ItemRepository itemRepository;
    private final CompanyRepository companyRepository;
    private final GstCalculator gst;

    public record ReceiveLine(Integer productID, String batchNo, LocalDate expiryDate,
                              BigDecimal mrp, BigDecimal quantity,
                              BigDecimal purchaseRate, BigDecimal gstRate) {}

    @Transactional
    public Purchase receive(Integer vendorID, String vendorBillNo, LocalDate date,
                            List<ReceiveLine> lines, Integer userId) {

        Vendor vendor = vendorRepository.findById(vendorID)
                .orElseThrow(() -> new IllegalStateException("Vendor not found"));

        Purchase purchase = Purchase.builder()
                .purchaseNo(nextPurchaseNo())
                .purchaseDate(date != null ? date : LocalDate.now())
                .vendor(vendor)
                .vendorBillNo(vendorBillNo)
                .createdBy(userId)
                .build();

        BigDecimal sub = BigDecimal.ZERO, tax = BigDecimal.ZERO;

        for (ReceiveLine line : lines) {
            Item item = itemRepository.findById(line.productID())
                    .orElseThrow(() -> new IllegalStateException("Item not found: " + line.productID()));

            BigDecimal gstRate = line.gstRate() != null ? line.gstRate() : item.getGstRate();
            var lt = gst.computeLine(line.purchaseRate(), BigDecimal.ZERO,
                    line.quantity(), gstRate);
            sub = sub.add(lt.taxable());
            tax = tax.add(lt.tax());

            // Create or find batch
            Batch batch = batchRepository
                    .findByProductProductIDAndBatchNoAndExpiryDate(
                            item.getProductID(), line.batchNo(), line.expiryDate())
                    .orElseGet(() -> Batch.builder()
                            .product(item)
                            .batchNo(line.batchNo())
                            .expiryDate(line.expiryDate())
                            .mrp(line.mrp())
                            .purchaseRate(line.purchaseRate())
                            .saleRate(line.purchaseRate())
                            .qtyIn(BigDecimal.ZERO)
                            .qtyBalance(BigDecimal.ZERO)
                            .vendor(vendor)
                            .purchaseDate(purchase.getPurchaseDate())
                            .build());

            batch.setQtyIn(batch.getQtyIn().add(line.quantity()));
            batch.setQtyBalance(batch.getQtyBalance().add(line.quantity()));
            batch = batchRepository.save(batch);

            purchase.addItem(PurchaseItem.builder()
                    .product(item)
                    .batch(batch)
                    .batchNo(line.batchNo())
                    .expiryDate(line.expiryDate())
                    .mrp(line.mrp())
                    .quantity(line.quantity())
                    .purchaseRate(line.purchaseRate())
                    .gstRate(gstRate)
                    .taxableValue(lt.taxable())
                    .taxAmount(lt.tax())
                    .lineTotal(lt.taxable().add(lt.tax()))
                    .build());

            ledgerRepository.save(StockLedger.builder()
                    .batch(batch)
                    .product(item)
                    .txnType("PURCHASE")
                    .refID(purchase.getPurchaseID())    // will be set after persist
                    .qtyChange(line.quantity())
                    .balanceAfter(batch.getQtyBalance())
                    .build());
        }

        // GST split — vendor state vs company state
        String companyState = companyRepository.findAll().stream()
                .findFirst().map(Company::getState).orElse("Maharashtra");
        boolean inter = !vendor.getState().equalsIgnoreCase(companyState);

        purchase.setSubTotal(sub);
        purchase.setCgst(inter ? BigDecimal.ZERO : tax.divide(new BigDecimal("2"), 2, java.math.RoundingMode.HALF_UP));
        purchase.setSgst(inter ? BigDecimal.ZERO : tax.subtract(purchase.getCgst()));
        purchase.setIgst(inter ? tax : BigDecimal.ZERO);
        purchase.setGrandTotal(sub.add(tax).setScale(2, java.math.RoundingMode.HALF_UP));

        return purchaseRepository.save(purchase);
    }

    private String nextPurchaseNo() {
        long n = purchaseRepository.count() + 1;
        return String.format("PUR-%d-%04d", LocalDate.now().getYear(), n);
    }
}
