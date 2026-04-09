package com.smartinventory.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Kullanıcı bilgi yanıtı")
public class UserResponse {

    @Schema(description = "Kullanıcı ID", example = "1")
    private Long id;

    @Schema(description = "Ad", example = "Ahmet")
    private String firstName;

    @Schema(description = "Soyad", example = "Yılmaz")
    private String lastName;

    @Schema(description = "E-posta", example = "ahmet@sirket.com")
    private String email;

    @Schema(description = "Departman", example = "Üretim")
    private String department;

    @Schema(description = "Rol", example = "WAREHOUSE_MANAGER")
    private String role;

    @Schema(description = "Aktif mi", example = "true")
    private boolean active;
}
