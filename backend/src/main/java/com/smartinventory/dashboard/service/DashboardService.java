package com.smartinventory.dashboard.service;

import com.smartinventory.dashboard.dto.DashboardSummaryResponse;
import com.smartinventory.dashboard.dto.DashboardSummaryResponse.LowStockProduct;
import com.smartinventory.dashboard.dto.DashboardSummaryResponse.RecentMovement;
import com.smartinventory.materialrequest.enums.MaterialRequestStatus;
import com.smartinventory.materialrequest.repository.MaterialRequestRepository;
import com.smartinventory.product.entity.Product;
import com.smartinventory.product.repository.ProductRepository;
import com.smartinventory.purchaseorder.enums.PurchaseOrderStatus;
import com.smartinventory.purchaseorder.repository.PurchaseOrderRepository;
import com.smartinventory.stockmovement.entity.StockMovement;
import com.smartinventory.stockmovement.repository.StockMovementRepository;
import com.smartinventory.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private static final int LOW_STOCK_LIMIT = 5;
    private static final int RECENT_MOVEMENTS_LIMIT = 5;

    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final MaterialRequestRepository materialRequestRepository;

    public DashboardSummaryResponse getSummary() {
        BigDecimal totalStockValue = productRepository.sumActiveStockValue();
        if (totalStockValue == null) {
            totalStockValue = BigDecimal.ZERO;
        }

        List<LowStockProduct> lowStockProducts = productRepository.findLowStockProducts().stream()
                .limit(LOW_STOCK_LIMIT)
                .map(this::toLowStockProduct)
                .toList();

        List<RecentMovement> recentMovements = stockMovementRepository
                .findAllByOrderByCreatedAtDesc(PageRequest.of(0, RECENT_MOVEMENTS_LIMIT))
                .stream()
                .map(this::toRecentMovement)
                .toList();

        return DashboardSummaryResponse.builder()
                .totalActiveProducts(productRepository.countByActiveTrue())
                .totalStockValue(totalStockValue)
                .lowStockCount(productRepository.countLowStockProducts())
                .pendingMaterialRequests(
                        materialRequestRepository.countByStatus(MaterialRequestStatus.PENDING))
                .draftPurchaseOrders(
                        purchaseOrderRepository.countByStatus(PurchaseOrderStatus.DRAFT))
                .sentPurchaseOrders(
                        purchaseOrderRepository.countByStatus(PurchaseOrderStatus.SENT))
                .lowStockProducts(lowStockProducts)
                .recentMovements(recentMovements)
                .build();
    }

    private LowStockProduct toLowStockProduct(Product product) {
        return LowStockProduct.builder()
                .id(product.getId())
                .name(product.getName())
                .currentStock(product.getCurrentStock())
                .minimumStockLevel(product.getMinimumStockLevel())
                .build();
    }

    private RecentMovement toRecentMovement(StockMovement movement) {
        Product product = movement.getProduct();
        User user = movement.getPerformedBy();
        return RecentMovement.builder()
                .id(movement.getId())
                .type(movement.getType())
                .quantity(movement.getQuantity())
                .productName(product != null ? product.getName() : null)
                .performedBy(user != null ? user.getFirstName() + " " + user.getLastName() : null)
                .createdAt(movement.getCreatedAt())
                .build();
    }
}
