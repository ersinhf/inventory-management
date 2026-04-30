package com.smartinventory.stockmovement.controller;

import com.smartinventory.stockmovement.dto.StockMovementRequest;
import com.smartinventory.stockmovement.dto.StockMovementResponse;
import com.smartinventory.stockmovement.enums.MovementType;
import com.smartinventory.stockmovement.service.StockMovementService;
import com.smartinventory.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/stock-movements")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Stok Hareketleri", description = "Stok giriş/çıkış/sayım hareketleri")
public class StockMovementController {

    private final StockMovementService stockMovementService;

    @GetMapping
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'DEPARTMENT_EMPLOYEE')")
    @Operation(
        summary = "Stok hareketlerini filtrele",
        description = "Ürün, tip ve tarih aralığına göre filtreleme. Tüm parametreler opsiyoneldir.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Başarılı")
        }
    )
    public ResponseEntity<List<StockMovementResponse>> filterMovements(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) MovementType type,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(stockMovementService.filter(productId, type, from, to));
    }

    @GetMapping("/product/{productId}")
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'DEPARTMENT_EMPLOYEE')")
    @Operation(
        summary = "Bir ürünün hareket geçmişi",
        responses = {
            @ApiResponse(responseCode = "200", description = "Başarılı"),
            @ApiResponse(responseCode = "404", description = "Ürün bulunamadı")
        }
    )
    public ResponseEntity<List<StockMovementResponse>> getByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(stockMovementService.getByProduct(productId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'DEPARTMENT_EMPLOYEE')")
    @Operation(
        summary = "Yeni stok hareketi oluştur",
        description = "IN/ADJUSTMENT yalnızca Depo Sorumlusu tarafından, OUT her iki rol tarafından yapılabilir.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Hareket kaydedildi"),
            @ApiResponse(responseCode = "400", description = "Yetersiz stok veya geçersiz istek"),
            @ApiResponse(responseCode = "403", description = "Yetkisiz işlem"),
            @ApiResponse(responseCode = "404", description = "Ürün bulunamadı")
        }
    )
    public ResponseEntity<StockMovementResponse> createMovement(
            @Valid @RequestBody StockMovementRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(stockMovementService.createMovement(request, currentUser));
    }
}
