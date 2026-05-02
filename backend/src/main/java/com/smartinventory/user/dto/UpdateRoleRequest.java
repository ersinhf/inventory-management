package com.smartinventory.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Kullanıcı rolünü güncelleme isteği")
public class UpdateRoleRequest {

    @Schema(description = "Yeni rol adı", example = "WAREHOUSE_MANAGER")
    @NotBlank(message = "Rol alanı boş olamaz")
    private String roleName;
}
