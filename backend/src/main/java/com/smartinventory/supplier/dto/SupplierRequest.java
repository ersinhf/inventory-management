package com.smartinventory.supplier.dto;

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
@Schema(description = "Tedarikçi oluşturma/güncelleme isteği")
public class SupplierRequest {

    @Schema(description = "Firma adı", example = "ABC Tedarik Ltd. Şti.")
    @NotBlank(message = "Firma adı boş olamaz")
    @Size(max = 150)
    private String name;

    @Schema(description = "Yetkili kişi", example = "Mehmet Kara")
    @Size(max = 100)
    private String contactPerson;

    @Schema(description = "E-posta", example = "info@abctedarik.com")
    @Email(message = "Geçerli bir e-posta adresi giriniz")
    @Size(max = 100)
    private String email;

    @Schema(description = "Telefon numarası", example = "+905551234567")
    @Size(max = 20)
    private String phone;

    @Schema(description = "Adres", example = "Ankara / Türkiye")
    @Size(max = 300)
    private String address;

    @Schema(description = "Vergi numarası", example = "1234567890")
    @Size(max = 20)
    private String taxNumber;
}
