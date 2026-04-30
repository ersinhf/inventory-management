package com.smartinventory.materialrequest.controller;

import com.smartinventory.materialrequest.dto.MaterialRequestDecisionRequest;
import com.smartinventory.materialrequest.dto.MaterialRequestRequest;
import com.smartinventory.materialrequest.dto.MaterialRequestResponse;
import com.smartinventory.materialrequest.enums.MaterialRequestStatus;
import com.smartinventory.materialrequest.service.MaterialRequestService;
import com.smartinventory.user.entity.User;
import com.smartinventory.user.enums.RoleName;
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
@RequestMapping("/api/v1/material-requests")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Malzeme Talepleri", description = "Bölüm çalışanlarının stok talep formu")
public class MaterialRequestController {

    private final MaterialRequestService materialRequestService;

    @GetMapping
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'DEPARTMENT_EMPLOYEE')")
    @Operation(
        summary = "Talepleri listele",
        description = "Depo Sorumlusu tüm talepleri görür. Bölüm Çalışanı sadece kendi taleplerini görür "
                + "(otomatik filtre uygulanır, parametre verilse bile yok sayılır).",
        responses = {
            @ApiResponse(responseCode = "200", description = "Başarılı")
        }
    )
    public ResponseEntity<List<MaterialRequestResponse>> filter(
            @RequestParam(required = false) MaterialRequestStatus status,
            @RequestParam(required = false) Long requesterId,
            @AuthenticationPrincipal User currentUser) {
        Long effectiveRequesterId = currentUser.getRole().getName() == RoleName.WAREHOUSE_MANAGER
                ? requesterId
                : currentUser.getId();
        return ResponseEntity.ok(materialRequestService.filter(status, effectiveRequesterId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'DEPARTMENT_EMPLOYEE')")
    @Operation(
        summary = "Talep detayı",
        description = "Bölüm Çalışanı sadece kendi talebini görebilir.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Başarılı"),
            @ApiResponse(responseCode = "403", description = "Yetkisiz"),
            @ApiResponse(responseCode = "404", description = "Talep bulunamadı")
        }
    )
    public ResponseEntity<MaterialRequestResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(materialRequestService.getById(id, currentUser));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'DEPARTMENT_EMPLOYEE')")
    @Operation(
        summary = "Yeni malzeme talebi oluştur",
        description = "Talep PENDING durumda oluşur, Depo Sorumlusu onayı bekler.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Talep oluşturuldu"),
            @ApiResponse(responseCode = "400", description = "Geçersiz istek"),
            @ApiResponse(responseCode = "404", description = "Ürün bulunamadı")
        }
    )
    public ResponseEntity<MaterialRequestResponse> create(
            @Valid @RequestBody MaterialRequestRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(materialRequestService.create(request, currentUser));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    @Operation(
        summary = "Talebi onayla (PENDING -> APPROVED)",
        description = "Onayda her satır için otomatik OUT stok hareketi oluşur. Yetersiz stok varsa onay reddedilir.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Onaylandı, stoklar güncellendi"),
            @ApiResponse(responseCode = "400", description = "Yetersiz stok veya geçersiz durum")
        }
    )
    public ResponseEntity<MaterialRequestResponse> approve(
            @PathVariable Long id,
            @RequestBody(required = false) MaterialRequestDecisionRequest decision,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(materialRequestService.approve(id, decision, currentUser));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    @Operation(
        summary = "Talebi reddet (PENDING -> REJECTED)",
        description = "Red gerekçesi (decisionNote) zorunludur.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Reddedildi"),
            @ApiResponse(responseCode = "400", description = "Gerekçe boş veya geçersiz durum")
        }
    )
    public ResponseEntity<MaterialRequestResponse> reject(
            @PathVariable Long id,
            @Valid @RequestBody MaterialRequestDecisionRequest decision,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(materialRequestService.reject(id, decision, currentUser));
    }
}
