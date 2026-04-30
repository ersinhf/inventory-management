package com.smartinventory.materialrequest.dto;

import com.smartinventory.materialrequest.enums.MaterialRequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Malzeme talebi yanıtı")
public class MaterialRequestResponse {

    @Schema(example = "10")
    private Long id;

    @Schema(example = "PENDING")
    private MaterialRequestStatus status;

    @Schema(example = "Pazarlama dep. — fuar standı için")
    private String reason;

    @Schema(example = "2026-04-30T20:30:00")
    private LocalDateTime createdAt;

    @Schema(example = "2026-05-01T09:15:00", nullable = true)
    private LocalDateTime decidedAt;

    @Schema(example = "Onaylandı, yarın hazırlanacak", nullable = true)
    private String decisionNote;

    @Schema(description = "Talebi oluşturan kullanıcı")
    private UserRef requestedBy;

    @Schema(description = "Karar veren kullanıcı (varsa)", nullable = true)
    private UserRef decidedBy;

    @Schema(description = "Talep edilen ürünler")
    private List<MaterialRequestItemResponse> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserRef {
        @Schema(example = "3")
        private Long id;

        @Schema(example = "Ahmet Yılmaz")
        private String fullName;

        @Schema(example = "Pazarlama")
        private String department;
    }
}
