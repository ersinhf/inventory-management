package com.smartinventory.materialrequest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Talep satırı")
public class MaterialRequestItemRequest {

    @Schema(example = "1")
    @NotNull(message = "Ürün seçilmelidir")
    private Long productId;

    @Schema(example = "5")
    @NotNull(message = "Miktar zorunlu")
    @Min(value = 1, message = "Miktar en az 1 olmalı")
    private Integer quantity;
}
