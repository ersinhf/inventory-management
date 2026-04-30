package com.smartinventory.purchaseorder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Satın alma siparişi oluşturma / güncelleme isteği")
public class PurchaseOrderRequest {

    @Schema(example = "1")
    @NotNull(message = "Tedarikçi seçilmelidir")
    private Long supplierId;

    @Schema(example = "Aylık rutin sipariş")
    @Size(max = 500)
    private String note;

    @Valid
    @NotEmpty(message = "Sipariş en az bir satır içermeli")
    private List<PurchaseOrderItemRequest> items;
}
