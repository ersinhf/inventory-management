package com.smartinventory.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Kullanıcı bilgilerini güncelleme isteği")
public class UpdateUserRequest {

    @Schema(description = "Ad", example = "Ahmet")
    @NotBlank(message = "Ad alanı boş olamaz")
    @Size(max = 50)
    private String firstName;

    @Schema(description = "Soyad", example = "Yılmaz")
    @NotBlank(message = "Soyad alanı boş olamaz")
    @Size(max = 50)
    private String lastName;

    @Schema(description = "E-posta adresi", example = "ahmet@sirket.com")
    @NotBlank(message = "E-posta alanı boş olamaz")
    @Email(message = "Geçerli bir e-posta adresi giriniz")
    private String email;

    @Schema(description = "Departman", example = "Üretim")
    @Size(max = 100)
    private String department;
}
