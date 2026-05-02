package com.smartinventory.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "Tedarikçi bazlı toplam harcama satırı")
public class SupplierTotalRow {

    private Long supplierId;
    private String supplierName;
    private Long orderCount;
    private BigDecimal totalAmount;

    public SupplierTotalRow(Long supplierId, String supplierName,
                            Long orderCount, BigDecimal totalAmount) {
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.orderCount = orderCount != null ? orderCount : 0L;
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }
}
