package com.smartinventory.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Ürün oluşturma/güncelleme isteği")
public class ProductRequest {

    @Schema(description = "Ürün adı", example = "A4 Fotokopi Kağıdı")
    @NotBlank(message = "Ürün adı boş olamaz")
    @Size(max = 150)
    private String name;

    @Schema(description = "Açıklama", example = "80 gr, 500 yaprak paket")
    @Size(max = 500)
    private String description;

    @Schema(description = "Barkod", example = "8690000000001")
    @NotBlank(message = "Barkod boş olamaz")
    @Size(max = 50)
    private String barcode;

    @Schema(description = "Birim fiyat", example = "85.50")
    @NotNull(message = "Birim fiyat boş olamaz")
    @DecimalMin(value = "0.0", inclusive = true, message = "Birim fiyat negatif olamaz")
    private BigDecimal unitPrice;

    @Schema(description = "Minimum stok seviyesi (uyarı eşiği)", example = "10")
    @NotNull(message = "Minimum stok seviyesi boş olamaz")
    @Min(value = 0, message = "Minimum stok negatif olamaz")
    private Integer minimumStockLevel;

    @Schema(description = "Kategori ID (opsiyonel)", example = "1")
    private Long categoryId;

    @Schema(description = "Tedarikçi ID listesi", example = "[1, 2]")
    private Set<Long> supplierIds;
}
