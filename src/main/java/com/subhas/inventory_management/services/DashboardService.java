package com.subhas.inventory_management.services;

import com.subhas.inventory_management.entity.Batch;
import com.subhas.inventory_management.entity.Item;
import com.subhas.inventory_management.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final BatchRepository batchRepository;
    private final ItemRepository itemRepository;
    private final InvoiceRepository invoiceRepository;

    @Value("${app.expiry.alert-days:90}")
    private int alertDays;

    public record LowStock(Item item, BigDecimal totalStock) {}
    public record TodaySummary(BigDecimal sales, long invoiceCount) {}

    public List<Batch> expiringBatches() {
        return batchRepository.findExpiringBefore(LocalDate.now().plusDays(alertDays));
    }

    public List<LowStock> lowStockItems() {
        List<LowStock> out = new ArrayList<>();
        for (Item i : itemRepository.findByStatus("Active")) {
            BigDecimal s = batchRepository.totalStock(i.getProductID());
            if (s.compareTo(BigDecimal.valueOf(i.getReorderLevel())) < 0) {
                out.add(new LowStock(i, s));
            }
        }
        return out;
    }

    public TodaySummary todaySummary() {
        LocalDate today = LocalDate.now();
        return new TodaySummary(
                invoiceRepository.totalForDate(today),
                invoiceRepository.countForDate(today));
    }
}
