package com.smartinventory.dashboard.dto;

import com.smartinventory.stockmovement.enums.MovementType;
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
@Schema(description = "Ana panel özet bilgisi")
public class DashboardSummaryResponse {

    @Schema(description = "Aktif ürün sayısı", example = "42")
    private long totalActiveProducts;

    @Schema(description = "Aktif ürünlerin toplam stok değeri", example = "125450.75")
    private BigDecimal totalStockValue;

    @Schema(description = "Kritik (minimum seviyede ya da altında) ürün sayısı", example = "5")
    private long lowStockCount;

    @Schema(description = "Onay bekleyen malzeme talebi sayısı", example = "3")
    private long pendingMaterialRequests;

    @Schema(description = "Taslak satın alma siparişi sayısı", example = "2")
    private long draftPurchaseOrders;

    @Schema(description = "Gönderilmiş ve teslim alınmayı bekleyen satın alma siparişi sayısı", example = "4")
    private long sentPurchaseOrders;

    @Schema(description = "Kritik stok seviyesindeki ürünler (en fazla 5)")
    private List<LowStockProduct> lowStockProducts;

    @Schema(description = "Son stok hareketleri (en fazla 5)")
    private List<RecentMovement> recentMovements;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Kritik stok ürün özeti")
    public static class LowStockProduct {
        @Schema(example = "1")
        private Long id;

        @Schema(example = "A4 Fotokopi Kağıdı")
        private String name;

        @Schema(example = "5")
        private Integer currentStock;

        @Schema(example = "10")
        private Integer minimumStockLevel;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Schema(description = "Son hareket özeti")
    public static class RecentMovement {
        @Schema(example = "12")
        private Long id;

        @Schema(example = "IN")
        private MovementType type;

        @Schema(example = "50")
        private Integer quantity;

        @Schema(example = "A4 Fotokopi Kağıdı")
        private String productName;

        @Schema(example = "Ahmet Yılmaz")
        private String performedBy;

        @Schema(example = "2026-04-30T20:15:00")
        private LocalDateTime createdAt;
    }
}
