package com.smartinventory.report.service;

import com.smartinventory.product.repository.ProductRepository;
import com.smartinventory.purchaseorder.repository.PurchaseOrderRepository;
import com.smartinventory.report.dto.CurrentStockRow;
import com.smartinventory.report.dto.PurchaseSummaryRow;
import com.smartinventory.report.dto.SupplierTotalRow;
import com.smartinventory.report.dto.TopMoverRow;
import com.smartinventory.stockmovement.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;

    public List<CurrentStockRow> getCurrentStock() {
        return productRepository.findAllActiveWithCategory().stream()
                .map(p -> {
                    int stock = p.getCurrentStock() != null ? p.getCurrentStock() : 0;
                    BigDecimal unit = p.getUnitPrice() != null ? p.getUnitPrice() : BigDecimal.ZERO;
                    BigDecimal totalValue = unit.multiply(BigDecimal.valueOf(stock));
                    String categoryName =
                            p.getCategory() != null && p.getCategory().getName() != null
                                    ? p.getCategory().getName()
                                    : "";
                    return CurrentStockRow.builder()
                            .productId(p.getId())
                            .barcode(p.getBarcode())
                            .name(p.getName())
                            .categoryName(categoryName)
                            .currentStock(stock)
                            .minimumStockLevel(
                                    p.getMinimumStockLevel() != null ? p.getMinimumStockLevel() : 0)
                            .unitPrice(unit)
                            .totalValue(totalValue)
                            .lowStock(p.isLowStock())
                            .build();
                })
                .toList();
    }

    public List<TopMoverRow> getTopMovers(LocalDateTime from, LocalDateTime to, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);
        return stockMovementRepository.findTopMovers(
                from, to, PageRequest.of(0, safeLimit));
    }

    public List<SupplierTotalRow> getSupplierTotals(LocalDateTime from, LocalDateTime to) {
        return purchaseOrderRepository.findSupplierTotals(from, to);
    }

    public List<PurchaseSummaryRow> getPurchaseSummary(LocalDateTime from, LocalDateTime to) {
        return purchaseOrderRepository.findForPurchaseReport(from, to).stream()
                .map(po -> PurchaseSummaryRow.builder()
                        .orderId(po.getId())
                        .supplierName(po.getSupplier().getName())
                        .status(po.getStatus().name())
                        .totalAmount(po.getTotalAmount())
                        .createdAt(po.getCreatedAt())
                        .sentAt(po.getSentAt())
                        .receivedAt(po.getReceivedAt())
                        .itemCount(po.getItems() != null ? po.getItems().size() : 0)
                        .build())
                .toList();
    }
}
