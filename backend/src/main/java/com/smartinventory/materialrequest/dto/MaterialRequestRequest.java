package com.smartinventory.materialrequest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Malzeme talep formu oluşturma isteği")
public class MaterialRequestRequest {

    @Schema(description = "Talep gerekçesi (hangi departman, ne için)",
            example = "Pazarlama dep. — fuar standı için")
    @Size(max = 500)
    private String reason;

    @Valid
    @NotEmpty(message = "En az bir ürün talep edilmelidir")
    private List<MaterialRequestItemRequest> items;
}
