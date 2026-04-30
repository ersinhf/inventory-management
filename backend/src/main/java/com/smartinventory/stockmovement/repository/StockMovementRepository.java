package com.smartinventory.stockmovement.repository;

import com.smartinventory.stockmovement.entity.StockMovement;
import com.smartinventory.stockmovement.enums.MovementType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @Query("""
            SELECT m FROM StockMovement m
            WHERE (:productId IS NULL OR m.product.id = :productId)
              AND (:type IS NULL OR m.type = :type)
              AND (:from IS NULL OR m.createdAt >= :from)
              AND (:to IS NULL OR m.createdAt <= :to)
            ORDER BY m.createdAt DESC
            """)
    List<StockMovement> filter(
            @Param("productId") Long productId,
            @Param("type") MovementType type,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    List<StockMovement> findByProductIdOrderByCreatedAtDesc(Long productId);

    List<StockMovement> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
