package com.subhas.inventory_management.controllers;

import com.subhas.inventory_management.dtos.ItemDto;
import com.subhas.inventory_management.entity.Item;
import com.subhas.inventory_management.exception.ApiException;
import com.subhas.inventory_management.repository.BatchRepository;
import com.subhas.inventory_management.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemRepository itemRepository;
    private final BatchRepository batchRepository;

    // ---------- GET: list / search ----------
    @GetMapping
    @Transactional(readOnly = true)
    public List<ItemDto> list(@RequestParam(required = false) String q,
                              @RequestParam(required = false) String status) {
        List<Item> items;
        if (q != null && !q.isBlank()) {
            items = itemRepository.searchByName(q);
        } else if (status != null && !status.isBlank()) {
            items = itemRepository.findByStatus(status);
        } else {
            items = itemRepository.findAll();
        }
        return items.stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ItemDto get(@PathVariable Integer id) {
        return toDto(find(id));
    }

    // ---------- Barcode lookup (for scanner) ----------
    @GetMapping("/barcode/{code}")
    @Transactional(readOnly = true)
    public ResponseEntity<ItemDto> byBarcode(@PathVariable String code) {
        Item item = itemRepository.findByBarcode(code)
                .orElseThrow(() -> new ApiException(
                        "No item mapped to barcode " + code, HttpStatus.NOT_FOUND));

        // REFUSE scanning inactive items
        if (!"Active".equalsIgnoreCase(item.getStatus())) {
            throw new ApiException(
                    "Item '" + item.getItemName() + "' is inactive and cannot be billed.",
                    HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(toDto(item));
    }

    // ---------- POST: create ----------
    @PostMapping
    public ResponseEntity<ItemDto> create(@RequestBody ItemDto dto) {
        Item saved = itemRepository.save(apply(new Item(), dto));
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    // ---------- PUT: update (does NOT change status) ----------
    @PutMapping("/{id}")
    public ItemDto update(@PathVariable Integer id, @RequestBody ItemDto dto) {
        Item existing = find(id);
        // status is intentionally ignored here — use PATCH /status
        Item saved = itemRepository.save(apply(existing, dto));
        return toDto(saved);
    }

    // ---------- PATCH: set status explicitly (activate / deactivate) ----------
    @PatchMapping("/{id}/status")
    public ItemDto setStatus(@PathVariable Integer id,
                             @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        if (newStatus == null) {
            throw new ApiException("Field 'status' is required", HttpStatus.BAD_REQUEST);
        }
        newStatus = newStatus.trim();
        if (!newStatus.equalsIgnoreCase("Active") && !newStatus.equalsIgnoreCase("Inactive")) {
            throw new ApiException("status must be 'Active' or 'Inactive'", HttpStatus.BAD_REQUEST);
        }
        Item item = find(id);
        item.setStatus(
                newStatus.equalsIgnoreCase("Active") ? "Active" : "Inactive"
        );
        Item saved = itemRepository.save(item);
        return toDto(saved);
    }

    // ---------- DELETE: soft-delete (set status INACTIVE) ----------
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Integer id) {
        Item item = find(id);
        item.setStatus("Inactive");
        itemRepository.save(item);
        return ResponseEntity.noContent().build();
    }

    // ---------- Helpers ----------
    private Item find(Integer id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new ApiException("Item not found: " + id, HttpStatus.NOT_FOUND));
    }

    private Item apply(Item target, ItemDto dto) {
        target.setItemNumber(dto.getItemNumber());
        target.setBarcode(dto.getBarcode());
        target.setItemName(dto.getItemName());
        target.setDescription(dto.getDescription());
        target.setHsnCode(dto.getHsnCode());
        if (dto.getGstRate() != null)      target.setGstRate(dto.getGstRate());
        if (dto.getUnit() != null)         target.setUnit(dto.getUnit());
        if (dto.getUnitPrice() != null)    target.setUnitPrice(dto.getUnitPrice());
        if (dto.getDiscount() != null)     target.setDiscount(dto.getDiscount());
        if (dto.getReorderLevel() != null) target.setReorderLevel(dto.getReorderLevel());
        target.setImageURL(dto.getImageURL());

        // status is NOT set here — only via PATCH /status or DELETE
        // (create() sets "Active" via the entity's @Builder.Default)

        return target;
    }

    private ItemDto toDto(Item i) {
        BigDecimal stock = batchRepository.totalStock(i.getProductID());
        return ItemDto.builder()
                .productID(i.getProductID())
                .itemNumber(i.getItemNumber())
                .barcode(i.getBarcode())
                .itemName(i.getItemName())
                .description(i.getDescription())
                .hsnCode(i.getHsnCode())
                .gstRate(i.getGstRate())
                .unit(i.getUnit())
                .unitPrice(i.getUnitPrice())
                .discount(i.getDiscount())
                .reorderLevel(i.getReorderLevel())
                .imageURL(i.getImageURL())
                .status(i.getStatus())
                .totalStock(stock)
                .build();
    }
}