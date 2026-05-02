package com.smartinventory.report.controller;

import com.smartinventory.report.dto.CurrentStockRow;
import com.smartinventory.report.dto.PurchaseSummaryRow;
import com.smartinventory.report.dto.SupplierTotalRow;
import com.smartinventory.report.dto.TopMoverRow;
import com.smartinventory.report.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Raporlar", description = "Depo sorumlusu raporları")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/current-stock")
    @Operation(
            summary = "Güncel stok raporu",
            description = "Aktif ürünlerin stok düzeyi ve değeri (sadece Depo Sorumlusu)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Başarılı"),
                    @ApiResponse(responseCode = "403", description = "Yetkisiz")
            })
    public ResponseEntity<List<CurrentStockRow>> currentStock() {
        return ResponseEntity.ok(reportService.getCurrentStock());
    }

    @GetMapping("/top-movers")
    @Operation(
            summary = "En çok hareket gören ürünler",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Başarılı"),
                    @ApiResponse(responseCode = "403", description = "Yetkisiz")
            })
    public ResponseEntity<List<TopMoverRow>> topMovers(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(name = "limit", required = false, defaultValue = "20") int limit) {
        return ResponseEntity.ok(reportService.getTopMovers(from, to, limit));
    }

    @GetMapping("/supplier-totals")
    @Operation(
            summary = "Tedarikçi bazlı satın alma toplamları",
            description = "İptal edilmemiş siparişler üzerinden",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Başarılı"),
                    @ApiResponse(responseCode = "403", description = "Yetkisiz")
            })
    public ResponseEntity<List<SupplierTotalRow>> supplierTotals(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(reportService.getSupplierTotals(from, to));
    }

    @GetMapping("/purchase-summary")
    @Operation(
            summary = "Satın alma siparişleri özeti",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Başarılı"),
                    @ApiResponse(responseCode = "403", description = "Yetkisiz")
            })
    public ResponseEntity<List<PurchaseSummaryRow>> purchaseSummary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(reportService.getPurchaseSummary(from, to));
    }
}
