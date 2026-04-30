package com.smartinventory.purchaseorder.service;

import com.smartinventory.common.exception.ResourceNotFoundException;
import com.smartinventory.product.entity.Product;
import com.smartinventory.product.repository.ProductRepository;
import com.smartinventory.purchaseorder.dto.PurchaseOrderItemRequest;
import com.smartinventory.purchaseorder.dto.PurchaseOrderItemResponse;
import com.smartinventory.purchaseorder.dto.PurchaseOrderRequest;
import com.smartinventory.purchaseorder.dto.PurchaseOrderResponse;
import com.smartinventory.purchaseorder.entity.PurchaseOrder;
import com.smartinventory.purchaseorder.entity.PurchaseOrderItem;
import com.smartinventory.purchaseorder.enums.PurchaseOrderStatus;
import com.smartinventory.purchaseorder.repository.PurchaseOrderRepository;
import com.smartinventory.stockmovement.enums.MovementType;
import com.smartinventory.stockmovement.service.StockMovementService;
import com.smartinventory.supplier.entity.Supplier;
import com.smartinventory.supplier.repository.SupplierRepository;
import com.smartinventory.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final StockMovementService stockMovementService;

    public List<PurchaseOrderResponse> filter(PurchaseOrderStatus status) {
        return purchaseOrderRepository.filterByStatus(status).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public PurchaseOrderResponse getById(Long id) {
        return mapToResponse(findOrder(id));
    }

    @Transactional
    public PurchaseOrderResponse create(PurchaseOrderRequest request, User currentUser) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier", "id", request.getSupplierId()));
        if (!supplier.isActive()) {
            throw new IllegalArgumentException("Pasif tedarikçiye sipariş açılamaz");
        }

        PurchaseOrder order = PurchaseOrder.builder()
                .supplier(supplier)
                .status(PurchaseOrderStatus.DRAFT)
                .note(request.getNote())
                .createdBy(currentUser)
                .totalAmount(BigDecimal.ZERO)
                .build();

        attachItems(order, request.getItems());
        order.setTotalAmount(calculateTotal(order));

        return mapToResponse(purchaseOrderRepository.save(order));
    }

    @Transactional
    public PurchaseOrderResponse update(Long id, PurchaseOrderRequest request) {
        PurchaseOrder order = findOrder(id);
        requireStatus(order, PurchaseOrderStatus.DRAFT, "Sadece taslak siparişler düzenlenebilir");

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Supplier", "id", request.getSupplierId()));
        if (!supplier.isActive()) {
            throw new IllegalArgumentException("Pasif tedarikçiye sipariş açılamaz");
        }

        order.setSupplier(supplier);
        order.setNote(request.getNote());
        order.clearItems();
        attachItems(order, request.getItems());
        order.setTotalAmount(calculateTotal(order));

        return mapToResponse(purchaseOrderRepository.save(order));
    }

    @Transactional
    public PurchaseOrderResponse send(Long id) {
        PurchaseOrder order = findOrder(id);
        requireStatus(order, PurchaseOrderStatus.DRAFT, "Sadece taslak siparişler gönderilebilir");
        order.setStatus(PurchaseOrderStatus.SENT);
        order.setSentAt(LocalDateTime.now());
        return mapToResponse(purchaseOrderRepository.save(order));
    }

    @Transactional
    public PurchaseOrderResponse receive(Long id, User currentUser) {
        PurchaseOrder order = findOrder(id);
        requireStatus(order, PurchaseOrderStatus.SENT,
                "Sadece gönderilmiş siparişler teslim alınabilir");

        for (PurchaseOrderItem item : order.getItems()) {
            String note = "Sipariş #" + order.getId() + " teslim alındı";
            stockMovementService.recordSystemMovement(
                    item.getProduct(), MovementType.IN, item.getQuantity(), currentUser, note);
        }

        order.setStatus(PurchaseOrderStatus.RECEIVED);
        order.setReceivedAt(LocalDateTime.now());
        return mapToResponse(purchaseOrderRepository.save(order));
    }

    @Transactional
    public PurchaseOrderResponse cancel(Long id) {
        PurchaseOrder order = findOrder(id);
        if (order.getStatus() == PurchaseOrderStatus.RECEIVED
                || order.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Teslim alınmış veya iptal edilmiş sipariş tekrar iptal edilemez");
        }
        order.setStatus(PurchaseOrderStatus.CANCELLED);
        return mapToResponse(purchaseOrderRepository.save(order));
    }

    private PurchaseOrder findOrder(Long id) {
        return purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PurchaseOrder", "id", id));
    }

    private void requireStatus(PurchaseOrder order, PurchaseOrderStatus expected, String errorMsg) {
        if (order.getStatus() != expected) {
            throw new IllegalArgumentException(errorMsg + " (mevcut durum: " + order.getStatus() + ")");
        }
    }

    private void attachItems(PurchaseOrder order, List<PurchaseOrderItemRequest> itemRequests) {
        for (PurchaseOrderItemRequest itemReq : itemRequests) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product", "id", itemReq.getProductId()));
            if (!product.isActive()) {
                throw new IllegalArgumentException(
                        "Pasif ürün siparişe eklenemez: " + product.getName());
            }
            PurchaseOrderItem item = PurchaseOrderItem.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .unitPrice(itemReq.getUnitPrice())
                    .build();
            order.addItem(item);
        }
    }

    private BigDecimal calculateTotal(PurchaseOrder order) {
        return order.getItems().stream()
                .map(PurchaseOrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private PurchaseOrderResponse mapToResponse(PurchaseOrder order) {
        Long leadTimeDays = null;
        if (order.getStatus() == PurchaseOrderStatus.RECEIVED
                && order.getSentAt() != null && order.getReceivedAt() != null) {
            leadTimeDays = Duration.between(order.getSentAt(), order.getReceivedAt()).toDays();
        }

        List<PurchaseOrderItemResponse> itemResponses = order.getItems().stream()
                .map(this::mapItem)
                .toList();

        return PurchaseOrderResponse.builder()
                .id(order.getId())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .sentAt(order.getSentAt())
                .receivedAt(order.getReceivedAt())
                .totalAmount(order.getTotalAmount())
                .leadTimeDays(leadTimeDays)
                .note(order.getNote())
                .supplier(PurchaseOrderResponse.SupplierRef.builder()
                        .id(order.getSupplier().getId())
                        .name(order.getSupplier().getName())
                        .build())
                .createdBy(PurchaseOrderResponse.UserRef.builder()
                        .id(order.getCreatedBy().getId())
                        .fullName(order.getCreatedBy().getFirstName() + " "
                                + order.getCreatedBy().getLastName())
                        .build())
                .items(itemResponses)
                .build();
    }

    private PurchaseOrderItemResponse mapItem(PurchaseOrderItem item) {
        return PurchaseOrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productBarcode(item.getProduct().getBarcode())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .lineTotal(item.getLineTotal())
                .build();
    }
}
