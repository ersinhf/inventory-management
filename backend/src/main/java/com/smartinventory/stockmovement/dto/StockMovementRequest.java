package com.smartinventory.stockmovement.dto;

import com.smartinventory.stockmovement.enums.MovementType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Stok hareketi oluşturma isteği")
public class StockMovementRequest {

    @Schema(description = "Hareketin uygulanacağı ürün ID", example = "1")
    @NotNull(message = "Ürün seçilmelidir")
    private Long productId;

    @Schema(description = "Hareket tipi: IN (giriş), OUT (çıkış), ADJUSTMENT (sayım sonrası yeni stok)",
            example = "IN")
    @NotNull(message = "Hareket tipi zorunlu")
    private MovementType type;

    @Schema(description = "IN/OUT için adet, ADJUSTMENT için sayım sonrası YENİ toplam stok",
            example = "50")
    @NotNull(message = "Miktar zorunlu")
    @Min(value = 0, message = "Miktar 0 veya pozitif olmalı")
    private Integer quantity;

    @Schema(description = "Açıklama / not", example = "Tedarikçi #2'den mal kabul")
    @Size(max = 500)
    private String note;
}
