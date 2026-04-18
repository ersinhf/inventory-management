package com.smartinventory.supplier.controller;

import com.smartinventory.supplier.dto.SupplierRequest;
import com.smartinventory.supplier.dto.SupplierResponse;
import com.smartinventory.supplier.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tedarikçi Yönetimi", description = "Tedarikçi firmaların CRUD işlemleri")
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    @Operation(
        summary = "Tüm tedarikçileri listele",
        description = "Aktif ve pasif tüm tedarikçileri döner. Sadece Depo Sorumlusu erişebilir.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Başarılı"),
            @ApiResponse(responseCode = "403", description = "Yetkisiz erişim")
        }
    )
    public ResponseEntity<List<SupplierResponse>> getAllSuppliers(
            @RequestParam(name = "activeOnly", required = false, defaultValue = "false") boolean activeOnly) {
        List<SupplierResponse> suppliers = activeOnly
                ? supplierService.getActiveSuppliers()
                : supplierService.getAllSuppliers();
        return ResponseEntity.ok(suppliers);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    @Operation(
        summary = "ID ile tedarikçi getir",
        responses = {
            @ApiResponse(responseCode = "200", description = "Başarılı"),
            @ApiResponse(responseCode = "404", description = "Tedarikçi bulunamadı")
        }
    )
    public ResponseEntity<SupplierResponse> getSupplierById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    @Operation(
        summary = "Yeni tedarikçi oluştur",
        description = "Sadece Depo Sorumlusu yeni tedarikçi ekleyebilir.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Tedarikçi oluşturuldu"),
            @ApiResponse(responseCode = "400", description = "Geçersiz istek veya yinelenen kayıt")
        }
    )
    public ResponseEntity<SupplierResponse> createSupplier(
            @Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supplierService.createSupplier(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    @Operation(
        summary = "Tedarikçiyi güncelle",
        responses = {
            @ApiResponse(responseCode = "200", description = "Güncellendi"),
            @ApiResponse(responseCode = "404", description = "Tedarikçi bulunamadı"),
            @ApiResponse(responseCode = "400", description = "Geçersiz istek")
        }
    )
    public ResponseEntity<SupplierResponse> updateSupplier(
            @PathVariable Long id,
            @Valid @RequestBody SupplierRequest request) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    @Operation(
        summary = "Tedarikçiyi deaktif et",
        description = "Soft-delete: kaydı silmez, pasife çeker.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Deaktif edildi"),
            @ApiResponse(responseCode = "404", description = "Tedarikçi bulunamadı")
        }
    )
    public ResponseEntity<Void> deactivateSupplier(@PathVariable Long id) {
        supplierService.deactivateSupplier(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    @Operation(
        summary = "Tedarikçiyi tekrar aktif et",
        responses = {
            @ApiResponse(responseCode = "204", description = "Aktif edildi"),
            @ApiResponse(responseCode = "404", description = "Tedarikçi bulunamadı")
        }
    )
    public ResponseEntity<Void> activateSupplier(@PathVariable Long id) {
        supplierService.activateSupplier(id);
        return ResponseEntity.noContent().build();
    }
}
