package com.smartinventory.supplier.repository;

import com.smartinventory.supplier.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    Optional<Supplier> findByTaxNumber(String taxNumber);

    boolean existsByTaxNumber(String taxNumber);

    boolean existsByEmail(String email);

    List<Supplier> findAllByActiveTrue();
}
