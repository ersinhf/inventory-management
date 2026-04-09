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
@Schema(description = "Kullanıcı kayıt isteği")
public class RegisterRequest {

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

    @Schema(description = "Şifre (en az 6 karakter)", example = "sifre123")
    @NotBlank(message = "Şifre alanı boş olamaz")
    @Size(min = 6, max = 100, message = "Şifre en az 6 karakter olmalıdır")
    private String password;

    @Schema(description = "Departman", example = "Üretim")
    private String department;

    @Schema(description = "Rol adı", example = "DEPARTMENT_EMPLOYEE")
    @NotBlank(message = "Rol alanı boş olamaz")
    private String roleName;
}
