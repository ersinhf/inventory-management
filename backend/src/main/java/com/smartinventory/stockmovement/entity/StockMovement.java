package com.smartinventory.stockmovement.entity;

import com.smartinventory.common.entity.BaseEntity;
import com.smartinventory.product.entity.Product;
import com.smartinventory.stockmovement.enums.MovementType;
import com.smartinventory.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "stock_movements", indexes = {
    @Index(name = "idx_stockmovement_product", columnList = "product_id"),
    @Index(name = "idx_stockmovement_created_at", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MovementType type;

    /**
     * IN  -> stoğa eklenecek miktar (pozitif).
     * OUT -> stoktan düşülecek miktar (pozitif).
     * ADJUSTMENT -> sayım sonrası ürünün YENİ toplam stok değeri (pozitif/0).
     */
    @Column(nullable = false)
    private Integer quantity;

    /**
     * Hareket sonrası ürünün yeni stok değeri (audit için snapshot).
     */
    @Column(name = "stock_after", nullable = false)
    private Integer stockAfter;

    @Column(length = 500)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "performed_by_id", nullable = false)
    private User performedBy;

    /**
     * false -> hareket iptal edilmiş (pasif).
     * true  -> hareket aktif (varsayılan).
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;
}