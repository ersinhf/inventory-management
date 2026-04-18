package com.smartinventory.supplier.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Tedarikçi bilgi yanıtı")
public class SupplierResponse {

    @Schema(description = "Tedarikçi ID", example = "1")
    private Long id;

    @Schema(description = "Firma adı", example = "ABC Tedarik Ltd. Şti.")
    private String name;

    @Schema(description = "Yetkili kişi", example = "Mehmet Kara")
    private String contactPerson;

    @Schema(description = "E-posta", example = "info@abctedarik.com")
    private String email;

    @Schema(description = "Telefon", example = "+905551234567")
    private String phone;

    @Schema(description = "Adres", example = "Ankara / Türkiye")
    private String address;

    @Schema(description = "Vergi numarası", example = "1234567890")
    private String taxNumber;

    @Schema(description = "Aktif mi", example = "true")
    private boolean active;
}
