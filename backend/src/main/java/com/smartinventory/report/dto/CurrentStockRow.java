package com.smartinventory.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Mevcut stok raporu satırı")
public class CurrentStockRow {

    private Long productId;
    private String barcode;
    private String name;
    private String categoryName;
    private Integer currentStock;
    private Integer minimumStockLevel;
    private BigDecimal unitPrice;
    private BigDecimal totalValue;
    private boolean lowStock;
}
