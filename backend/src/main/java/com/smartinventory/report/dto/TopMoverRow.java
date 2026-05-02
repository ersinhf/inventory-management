package com.smartinventory.report.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@Schema(description = "En çok hareket gören ürün satırı")
public class TopMoverRow {

    private Long productId;
    private String barcode;
    private String name;
    private Long totalIn;
    private Long totalOut;
    private Long movementCount;

    public TopMoverRow(Long productId, String barcode, String name,
                       Long totalIn, Long totalOut, Long movementCount) {
        this.productId = productId;
        this.barcode = barcode;
        this.name = name;
        this.totalIn = totalIn != null ? totalIn : 0L;
        this.totalOut = totalOut != null ? totalOut : 0L;
        this.movementCount = movementCount != null ? movementCount : 0L;
    }

    public Long getTotalActivity() {
        return totalIn + totalOut;
    }
}
