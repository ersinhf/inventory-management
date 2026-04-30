package com.smartinventory.dashboard.controller;

import com.smartinventory.dashboard.dto.DashboardSummaryResponse;
import com.smartinventory.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasAnyRole('WAREHOUSE_MANAGER', 'DEPARTMENT_EMPLOYEE')")
@Tag(name = "Ana Panel", description = "Ana panel için özet istatistikler")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    @Operation(
        summary = "Panel özet bilgisi",
        description = "Toplam ürün sayısı, stok değeri, kritik stok adedi, bekleyen talep ve sipariş sayıları, kritik stok ürünler ve son hareketler.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Başarılı")
        }
    )
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }
}
