package com.smartinventory.purchaseorder.dto;

import com.smartinventory.purchaseorder.enums.PurchaseOrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Satın alma siparişi yanıtı")
public class PurchaseOrderResponse {

    @Schema(example = "12")
    private Long id;

    @Schema(example = "DRAFT")
    private PurchaseOrderStatus status;

    @Schema(description = "Tedarikçi özeti")
    private SupplierRef supplier;

    @Schema(example = "2026-04-30T20:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "Sipariş gönderim zamanı (SENT'e geçilince)", nullable = true)
    private LocalDateTime sentAt;

    @Schema(description = "Teslim alınma zamanı (RECEIVED'e geçilince)", nullable = true)
    private LocalDateTime receivedAt;

    @Schema(example = "12500.00")
    private BigDecimal totalAmount;

    @Schema(description = "Tedarik süresi (gün, RECEIVED ise hesaplı, değilse null)", nullable = true)
    private Long leadTimeDays;

    @Schema(example = "Aylık rutin sipariş")
    private String note;

    @Schema(description = "Siparişi oluşturan kullanıcı")
    private UserRef createdBy;

    @Schema(description = "Sipariş satırları")
    private List<PurchaseOrderItemResponse> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SupplierRef {
        @Schema(example = "1")
        private Long id;

        @Schema(example = "ABC Tedarik Ltd. Şti.")
        private String name;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserRef {
        @Schema(example = "1")
        private Long id;

        @Schema(example = "Ahmet Yılmaz")
        private String fullName;
    }
}
