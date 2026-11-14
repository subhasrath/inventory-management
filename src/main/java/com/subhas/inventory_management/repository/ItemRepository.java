package com.subhas.inventory_management.repository;

import com.subhas.inventory_management.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Integer> {

    Optional<Item> findByBarcode(String barcode);

    Optional<Item> findByItemNumber(String itemNumber);

    List<Item> findByStatus(String status);

    @Query("SELECT i FROM Item i WHERE LOWER(i.itemName) LIKE LOWER(CONCAT('%',:q,'%'))")
    List<Item> searchByName(@Param("q") String query);
}
