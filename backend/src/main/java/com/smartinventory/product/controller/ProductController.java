package com.smartinventory.product.controller;

import com.smartinventory.product.dto.ProductRequest;
import com.smartinventory.product.dto.ProductResponse;
import com.smartinventory.product.service.ProductService;
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
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Ürün Yönetimi", description = "Ürün/malzeme CRUD işlemleri ve barkod sorgusu")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'DEPARTMENT_EMPLOYEE')")
    @Operation(
        summary = "Tüm ürünleri listele",
        description = "Her iki rol de ürünleri görüntüleyebilir. activeOnly=true ile pasif ürünler hariç tutulur.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Başarılı")
        }
    )
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestParam(name = "activeOnly", required = false, defaultValue = "false") boolean activeOnly) {
        return ResponseEntity.ok(productService.getAllProducts(activeOnly));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'DEPARTMENT_EMPLOYEE')")
    @Operation(
        summary = "ID ile ürün getir",
        responses = {
            @ApiResponse(responseCode = "200", description = "Başarılı"),
            @ApiResponse(responseCode = "404", description = "Ürün bulunamadı")
        }
    )
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/barcode/{barcode}")
    @PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'DEPARTMENT_EMPLOYEE')")
    @Operation(
        summary = "Barkod ile ürün getir",
        description = "Barkod okuyucu ile hızlı sorgulama için kullanılır.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Başarılı"),
            @ApiResponse(responseCode = "404", description = "Bu barkodla ürün bulunamadı")
        }
    )
    public ResponseEntity<ProductResponse> getProductByBarcode(@PathVariable String barcode) {
        return ResponseEntity.ok(productService.getProductByBarcode(barcode));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    @Operation(
        summary = "Düşük stoklu ürünleri listele",
        description = "currentStock <= minimumStockLevel olan aktif ürünleri döner. Sadece Depo Sorumlusu erişebilir.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Başarılı")
        }
    )
    public ResponseEntity<List<ProductResponse>> getLowStockProducts() {
        return ResponseEntity.ok(productService.getLowStockProducts());
    }

    @PostMapping
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    @Operation(
        summary = "Yeni ürün oluştur",
        description = "Sadece Depo Sorumlusu yeni ürün ekleyebilir. Başlangıç stoku 0'dır; stok hareketleri ile güncellenir.",
        responses = {
            @ApiResponse(responseCode = "201", description = "Ürün oluşturuldu"),
            @ApiResponse(responseCode = "400", description = "Geçersiz istek veya yinelenen barkod"),
            @ApiResponse(responseCode = "404", description = "Bağlı kategori veya tedarikçi bulunamadı")
        }
    )
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    @Operation(
        summary = "Ürünü güncelle",
        description = "Stok adedi bu endpoint ile değişmez; stok hareketleri modülü üzerinden güncellenir.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Güncellendi"),
            @ApiResponse(responseCode = "404", description = "Ürün, kategori veya tedarikçi bulunamadı"),
            @ApiResponse(responseCode = "400", description = "Geçersiz istek")
        }
    )
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    @Operation(
        summary = "Ürünü deaktif et",
        description = "Soft-delete: kaydı silmez, pasife çeker.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Deaktif edildi"),
            @ApiResponse(responseCode = "404", description = "Ürün bulunamadı")
        }
    )
    public ResponseEntity<Void> deactivateProduct(@PathVariable Long id) {
        productService.deactivateProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('WAREHOUSE_MANAGER')")
    @Operation(
        summary = "Ürünü tekrar aktif et",
        responses = {
            @ApiResponse(responseCode = "204", description = "Aktif edildi"),
            @ApiResponse(responseCode = "404", description = "Ürün bulunamadı")
        }
    )
    public ResponseEntity<Void> activateProduct(@PathVariable Long id) {
        productService.activateProduct(id);
        return ResponseEntity.noContent().build();
    }
}
