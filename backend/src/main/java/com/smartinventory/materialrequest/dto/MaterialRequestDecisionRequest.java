package com.smartinventory.materialrequest.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Onay veya red için karar notu")
public class MaterialRequestDecisionRequest {

    @Schema(description = "Karar notu (red için zorunlu, onay için opsiyonel)",
            example = "Stok yeterli değil, sonraki dönem")
    @Size(max = 500)
    private String decisionNote;
}
