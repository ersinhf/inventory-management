package com.smartinventory.purchaseorder.repository;

import com.smartinventory.purchaseorder.entity.PurchaseOrder;
import com.smartinventory.purchaseorder.enums.PurchaseOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @Query("""
            SELECT po FROM PurchaseOrder po
            WHERE (:status IS NULL OR po.status = :status)
            ORDER BY po.createdAt DESC
            """)
    List<PurchaseOrder> filterByStatus(@Param("status") PurchaseOrderStatus status);
}
