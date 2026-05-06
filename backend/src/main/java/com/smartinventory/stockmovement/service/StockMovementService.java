package com.smartinventory.stockmovement.service;

import com.smartinventory.common.exception.ResourceNotFoundException;
import com.smartinventory.product.entity.Product;
import com.smartinventory.product.repository.ProductRepository;
import com.smartinventory.stockmovement.dto.StockMovementRequest;
import com.smartinventory.stockmovement.dto.StockMovementResponse;
import com.smartinventory.stockmovement.entity.StockMovement;
import com.smartinventory.stockmovement.enums.MovementType;
import com.smartinventory.stockmovement.repository.StockMovementRepository;
import com.smartinventory.user.entity.User;
import com.smartinventory.user.enums.RoleName;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StockMovementService {

    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;

    public List<StockMovementResponse> filter(
            Long productId, MovementType type, LocalDateTime from, LocalDateTime to) {
        return stockMovementRepository.filter(productId, type, from, to).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<StockMovementResponse> getByProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product", "id", productId);
        }
        return stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Sistem-içi (satın alma teslim alma, talep onayı vb.) tetiklenen stok hareketleri için
     * dahili kullanım. Rol kontrolünü dış modül yapmış olmalıdır.
     */
    @Transactional
    public void recordSystemMovement(
            Product product, MovementType type, int quantity, User performedBy, String note) {
        int newStock = applyMovement(product, type, quantity);
        product.setCurrentStock(newStock);
        productRepository.save(product);

        StockMovement movement = StockMovement.builder()
                .product(product)
                .type(type)
                .quantity(quantity)
                .stockAfter(newStock)
                .note(note)
                .performedBy(performedBy)
                .build();
        stockMovementRepository.save(movement);
    }

    @Transactional
    public StockMovementResponse createMovement(StockMovementRequest request, User currentUser) {
        validateRolePermission(currentUser, request.getType());

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product", "id", request.getProductId()));

        if (!product.isActive()) {
            throw new IllegalArgumentException(
                    "Pasif ürünler üzerinde stok hareketi yapılamaz: " + product.getName());
        }

        int newStock = applyMovement(product, request.getType(), request.getQuantity());
        product.setCurrentStock(newStock);
        productRepository.save(product);

        StockMovement movement = StockMovement.builder()
                .product(product)
                .type(request.getType())
                .quantity(request.getQuantity())
                .stockAfter(newStock)
                .note(request.getNote())
                .performedBy(currentUser)
                .build();

        return mapToResponse(stockMovementRepository.save(movement));
    }

    @Transactional
    public StockMovementResponse cancelMovement(Long id) {
        StockMovement movement = stockMovementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StockMovement", "id", id));

        if (!movement.isActive()) {
            throw new IllegalArgumentException("Bu hareket zaten iptal edilmiş");
        }

        movement.setActive(false);
        return mapToResponse(stockMovementRepository.save(movement));
    }

    private void validateRolePermission(User user, MovementType type) {
        RoleName role = user.getRole().getName();
        if (role == RoleName.WAREHOUSE_MANAGER) {
            return;
        }
        // DEPARTMENT_EMPLOYEE sadece OUT yapabilir.
        if (type != MovementType.OUT) {
            throw new AccessDeniedException(
                    "Bölüm Çalışanı sadece çıkış (OUT) hareketi yapabilir");
        }
    }

    private int applyMovement(Product product, MovementType type, int quantity) {
        int currentStock = product.getCurrentStock() == null ? 0 : product.getCurrentStock();
        return switch (type) {
            case IN -> {
                if (quantity <= 0) {
                    throw new IllegalArgumentException("Giriş miktarı 0'dan büyük olmalı");
                }
                yield currentStock + quantity;
            }
            case OUT -> {
                if (quantity <= 0) {
                    throw new IllegalArgumentException("Çıkış miktarı 0'dan büyük olmalı");
                }
                if (currentStock < quantity) {
                    throw new IllegalArgumentException(
                            "Yetersiz stok. Mevcut: " + currentStock + ", istenen: " + quantity);
                }
                yield currentStock - quantity;
            }
            case ADJUSTMENT -> {
                if (quantity < 0) {
                    throw new IllegalArgumentException("Sayım sonrası stok negatif olamaz");
                }
                yield quantity;
            }
        };
    }

    private StockMovementResponse mapToResponse(StockMovement movement) {
        Product product = movement.getProduct();
        User user = movement.getPerformedBy();

        return StockMovementResponse.builder()
                .id(movement.getId())
                .type(movement.getType())
                .quantity(movement.getQuantity())
                .stockAfter(movement.getStockAfter())
                .note(movement.getNote())
                .createdAt(movement.getCreatedAt())
                .active(movement.isActive())
                .product(StockMovementResponse.ProductRef.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .barcode(product.getBarcode())
                        .build())
                .performedBy(StockMovementResponse.UserRef.builder()
                        .id(user.getId())
                        .fullName(user.getFirstName() + " " + user.getLastName())
                        .build())
                .build();
    }
}
