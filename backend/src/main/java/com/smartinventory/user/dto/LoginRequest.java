package com.smartinventory.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Kullanıcı giriş isteği")
public class LoginRequest {

    @Schema(description = "E-posta adresi", example = "ahmet@sirket.com")
    @NotBlank(message = "E-posta alanı boş olamaz")
    @Email(message = "Geçerli bir e-posta adresi giriniz")
    private String email;

    @Schema(description = "Şifre", example = "sifre123")
    @NotBlank(message = "Şifre alanı boş olamaz")
    private String password;
}
