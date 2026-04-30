package com.smartinventory.materialrequest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Talep satırı yanıtı")
public class MaterialRequestItemResponse {

    @Schema(example = "5")
    private Long id;

    @Schema(example = "1")
    private Long productId;

    @Schema(example = "A4 Fotokopi Kağıdı")
    private String productName;

    @Schema(example = "8690000000001")
    private String productBarcode;

    @Schema(description = "Talep anında stoktaki miktar (snapshot)", example = "120")
    private Integer currentStock;

    @Schema(example = "5")
    private Integer quantity;
}
