package com.smartinventory.purchaseorder.repository;

import com.smartinventory.purchaseorder.entity.PurchaseOrder;
import com.smartinventory.purchaseorder.enums.PurchaseOrderStatus;
import com.smartinventory.report.dto.SupplierTotalRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @Query("""
            SELECT po FROM PurchaseOrder po
            WHERE (:status IS NULL OR po.status = :status)
            ORDER BY po.createdAt DESC
            """)
    List<PurchaseOrder> filterByStatus(@Param("status") PurchaseOrderStatus status);

    long countByStatus(PurchaseOrderStatus status);

    @Query("""
            SELECT po FROM PurchaseOrder po
            WHERE (:supplierId IS NULL OR po.supplier.id = :supplierId)
              AND (:status IS NULL OR po.status = :status)
              AND (:from IS NULL OR po.createdAt >= :from)
              AND (:to IS NULL OR po.createdAt <= :to)
            ORDER BY po.createdAt DESC
            """)
    List<PurchaseOrder> filterPurchases(
            @Param("supplierId") Long supplierId,
            @Param("status") PurchaseOrderStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("""
            SELECT new com.smartinventory.report.dto.SupplierTotalRow(
                s.id, s.name, COUNT(po), SUM(po.totalAmount)
            )
            FROM PurchaseOrder po JOIN po.supplier s
            WHERE po.status <> com.smartinventory.purchaseorder.enums.PurchaseOrderStatus.CANCELLED
              AND (:from IS NULL OR po.createdAt >= :from)
              AND (:to IS NULL OR po.createdAt <= :to)
            GROUP BY s.id, s.name
            ORDER BY SUM(po.totalAmount) DESC
            """)
    List<SupplierTotalRow> findSupplierTotals(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

    @Query("""
            SELECT DISTINCT po FROM PurchaseOrder po
            JOIN FETCH po.supplier
            LEFT JOIN FETCH po.items
            WHERE (:from IS NULL OR po.createdAt >= :from)
              AND (:to IS NULL OR po.createdAt <= :to)
            ORDER BY po.createdAt DESC
            """)
    List<PurchaseOrder> findForPurchaseReport(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
