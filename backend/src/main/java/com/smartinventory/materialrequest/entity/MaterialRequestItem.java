package com.smartinventory.materialrequest.entity;

import com.smartinventory.common.entity.BaseEntity;
import com.smartinventory.product.entity.Product;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "material_request_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialRequestItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_request_id", nullable = false)
    private MaterialRequest materialRequest;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;
}
