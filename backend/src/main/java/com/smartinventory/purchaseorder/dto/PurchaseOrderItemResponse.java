package com.smartinventory.purchaseorder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Sipariş satırı yanıtı")
public class PurchaseOrderItemResponse {

    @Schema(example = "5")
    private Long id;

    @Schema(example = "1")
    private Long productId;

    @Schema(example = "A4 Fotokopi Kağıdı")
    private String productName;

    @Schema(example = "8690000000001")
    private String productBarcode;

    @Schema(example = "100")
    private Integer quantity;

    @Schema(example = "85.50")
    private BigDecimal unitPrice;

    @Schema(example = "8550.00")
    private BigDecimal lineTotal;
}
