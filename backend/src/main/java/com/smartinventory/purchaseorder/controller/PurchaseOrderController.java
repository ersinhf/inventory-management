package com.smartinventory.purchaseorder.controller;

import com.smartinventory.purchaseorder.dto.PurchaseOrderRequest;
import com.smartinventory.purchaseorder.dto.PurchaseOrderResponse;
import com.smartinventory.purchaseorder.enums.PurchaseOrderStatus;
import com.smartinventory.purchaseorder.service.PurchaseOrderService;
import com.smartinventory.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/purchase-orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
@Tag(name = "Satın Alma Siparişleri", description = "Tedarikçilere satın alma siparişi oluşturma ve yönetme")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @GetMapping
    @Operation(
        summary = "Siparişleri listele",
        description = "İsteğe bağlı durum filtresi: DRAFT, SENT, RECEIVED, CANCELLED",
        responses = {
            @ApiResponse(responseCode = "200", description = "Başarılı")
        }
    )
    public ResponseEntity<List<PurchaseOrderResponse>> filter(
            @RequestParam(required = false) PurchaseOrderStatus status) {
        return ResponseEntity.ok(purchaseOrderService.filter(status));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Sipariş detayı",
        responses = {
            @ApiResponse(responseCode = "200", description = "Başarılı"),
            @ApiResponse(responseCode = "404", description = "Sipariş bulunamadı")
        }
    )
    public ResponseEntity<PurchaseOrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.getById(id));
    }

    @PostMapping
    @Operation(
        summary = "Yeni sipariş oluştur",
        description = "Yeni sipariş DRAFT durumda oluşturulur.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Sipariş oluşturuldu"),
            @ApiResponse(responseCode = "400", description = "Geçersiz istek"),
            @ApiResponse(responseCode = "404", description = "Tedarikçi veya ürün bulunamadı")
        }
    )
    public ResponseEntity<PurchaseOrderResponse> create(
            @Valid @RequestBody PurchaseOrderRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(purchaseOrderService.create(request, currentUser));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Siparişi güncelle (sadece DRAFT)",
        responses = {
            @ApiResponse(responseCode = "200", description = "Güncellendi"),
            @ApiResponse(responseCode = "400", description = "Sadece taslak güncellenebilir"),
            @ApiResponse(responseCode = "404", description = "Sipariş bulunamadı")
        }
    )
    public ResponseEntity<PurchaseOrderResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody PurchaseOrderRequest request) {
        return ResponseEntity.ok(purchaseOrderService.update(id, request));
    }

    @PatchMapping("/{id}/send")
    @Operation(
        summary = "Siparişi gönder (DRAFT -> SENT)",
        responses = {
            @ApiResponse(responseCode = "200", description = "Gönderildi"),
            @ApiResponse(responseCode = "400", description = "Sadece taslak gönderilebilir")
        }
    )
    public ResponseEntity<PurchaseOrderResponse> send(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.send(id));
    }

    @PatchMapping("/{id}/receive")
    @Operation(
        summary = "Siparişi teslim al (SENT -> RECEIVED)",
        description = "Her satır için otomatik olarak stok IN hareketi oluşturulur.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Teslim alındı, stoklar güncellendi"),
            @ApiResponse(responseCode = "400", description = "Sadece gönderilen siparişler teslim alınabilir")
        }
    )
    public ResponseEntity<PurchaseOrderResponse> receive(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(purchaseOrderService.receive(id, currentUser));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(
        summary = "Siparişi iptal et (DRAFT veya SENT)",
        responses = {
            @ApiResponse(responseCode = "200", description = "İptal edildi"),
            @ApiResponse(responseCode = "400", description = "Teslim alınmış veya iptal edilmiş sipariş iptal edilemez")
        }
    )
    public ResponseEntity<PurchaseOrderResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.cancel(id));
    }
}
