package com.smartinventory.purchaseorder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Sipariş satırı (oluşturma/güncelleme)")
public class PurchaseOrderItemRequest {

    @Schema(example = "1")
    @NotNull(message = "Ürün seçilmelidir")
    private Long productId;

    @Schema(example = "100")
    @NotNull(message = "Miktar zorunlu")
    @Min(value = 1, message = "Miktar en az 1 olmalı")
    private Integer quantity;

    @Schema(example = "85.50")
    @NotNull(message = "Birim fiyat zorunlu")
    @DecimalMin(value = "0.0", inclusive = true, message = "Birim fiyat negatif olamaz")
    private BigDecimal unitPrice;
}
