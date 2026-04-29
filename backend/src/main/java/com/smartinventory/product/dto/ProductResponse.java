package com.smartinventory.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Ürün bilgi yanıtı")
public class ProductResponse {

    @Schema(description = "Ürün ID", example = "1")
    private Long id;

    @Schema(description = "Ürün adı", example = "A4 Fotokopi Kağıdı")
    private String name;

    @Schema(description = "Açıklama")
    private String description;

    @Schema(description = "Barkod", example = "8690000000001")
    private String barcode;

    @Schema(description = "Birim fiyat", example = "85.50")
    private BigDecimal unitPrice;

    @Schema(description = "Mevcut stok adedi", example = "120")
    private Integer currentStock;

    @Schema(description = "Minimum stok seviyesi", example = "10")
    private Integer minimumStockLevel;

    @Schema(description = "Stok minimum seviyenin altında mı", example = "false")
    private boolean lowStock;

    @Schema(description = "Aktif mi", example = "true")
    private boolean active;

    @Schema(description = "Bağlı kategori (yoksa null)")
    private CategoryRef category;

    @Schema(description = "Tedarikçi listesi")
    private Set<SupplierRef> suppliers;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Kategori özeti")
    public static class CategoryRef {
        @Schema(example = "1")
        private Long id;

        @Schema(example = "Ofis Malzemeleri")
        private String name;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Tedarikçi özeti")
    public static class SupplierRef {
        @Schema(example = "1")
        private Long id;

        @Schema(example = "ABC Tedarik Ltd. Şti.")
        private String name;
    }
}
