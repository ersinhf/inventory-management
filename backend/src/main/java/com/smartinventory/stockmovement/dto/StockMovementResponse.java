package com.smartinventory.stockmovement.dto;

import com.smartinventory.stockmovement.enums.MovementType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Stok hareketi yanıtı")
public class StockMovementResponse {

    @Schema(example = "12")
    private Long id;

    @Schema(example = "IN")
    private MovementType type;

    @Schema(example = "50")
    private Integer quantity;

    @Schema(description = "Hareket sonrası ürün stoğu", example = "150")
    private Integer stockAfter;

    @Schema(example = "Tedarikçi #2'den mal kabul")
    private String note;

    @Schema(example = "2026-04-30T20:15:00")
    private LocalDateTime createdAt;

    @Schema(description = "Hareket yapılan ürünün özeti")
    private ProductRef product;

    @Schema(description = "Hareketi yapan kullanıcı")
    private UserRef performedBy;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Ürün özeti")
    public static class ProductRef {
        @Schema(example = "1")
        private Long id;

        @Schema(example = "A4 Fotokopi Kağıdı")
        private String name;

        @Schema(example = "8690000000001")
        private String barcode;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Kullanıcı özeti")
    public static class UserRef {
        @Schema(example = "3")
        private Long id;

        @Schema(example = "Ahmet Yılmaz")
        private String fullName;
    }
}
