package com.smartinventory.product.repository;

import com.smartinventory.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByBarcode(String barcode);

    boolean existsByBarcode(String barcode);

    List<Product> findAllByActiveTrue();

    @Query("SELECT p FROM Product p WHERE p.active = true AND p.currentStock <= p.minimumStockLevel")
    List<Product> findLowStockProducts();
}
