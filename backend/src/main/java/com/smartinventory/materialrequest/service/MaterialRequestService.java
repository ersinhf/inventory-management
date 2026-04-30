package com.smartinventory.materialrequest.service;

import com.smartinventory.common.exception.ResourceNotFoundException;
import com.smartinventory.materialrequest.dto.MaterialRequestDecisionRequest;
import com.smartinventory.materialrequest.dto.MaterialRequestItemRequest;
import com.smartinventory.materialrequest.dto.MaterialRequestItemResponse;
import com.smartinventory.materialrequest.dto.MaterialRequestRequest;
import com.smartinventory.materialrequest.dto.MaterialRequestResponse;
import com.smartinventory.materialrequest.entity.MaterialRequest;
import com.smartinventory.materialrequest.entity.MaterialRequestItem;
import com.smartinventory.materialrequest.enums.MaterialRequestStatus;
import com.smartinventory.materialrequest.repository.MaterialRequestRepository;
import com.smartinventory.product.entity.Product;
import com.smartinventory.product.repository.ProductRepository;
import com.smartinventory.stockmovement.enums.MovementType;
import com.smartinventory.stockmovement.service.StockMovementService;
import com.smartinventory.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaterialRequestService {

    private final MaterialRequestRepository materialRequestRepository;
    private final ProductRepository productRepository;
    private final StockMovementService stockMovementService;

    public List<MaterialRequestResponse> filter(MaterialRequestStatus status, Long requesterId) {
        return materialRequestRepository.filter(status, requesterId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public MaterialRequestResponse getById(Long id, User currentUser) {
        MaterialRequest request = findRequest(id);
        ensureAccess(request, currentUser);
        return mapToResponse(request);
    }

    @Transactional
    public MaterialRequestResponse create(MaterialRequestRequest request, User currentUser) {
        MaterialRequest mr = MaterialRequest.builder()
                .requestedBy(currentUser)
                .status(MaterialRequestStatus.PENDING)
                .reason(request.getReason())
                .build();

        attachItems(mr, request.getItems());

        return mapToResponse(materialRequestRepository.save(mr));
    }

    @Transactional
    public MaterialRequestResponse approve(
            Long id, MaterialRequestDecisionRequest decision, User decider) {
        MaterialRequest mr = findRequest(id);
        requirePending(mr);

        String note = "Talep #" + mr.getId() + " onaylandı"
                + (mr.getRequestedBy() != null
                    ? " — " + mr.getRequestedBy().getFirstName()
                        + " " + mr.getRequestedBy().getLastName()
                    : "");

        for (MaterialRequestItem item : mr.getItems()) {
            stockMovementService.recordSystemMovement(
                    item.getProduct(), MovementType.OUT, item.getQuantity(), decider, note);
        }

        mr.setStatus(MaterialRequestStatus.APPROVED);
        mr.setDecidedBy(decider);
        mr.setDecidedAt(LocalDateTime.now());
        mr.setDecisionNote(decision == null ? null : decision.getDecisionNote());

        return mapToResponse(materialRequestRepository.save(mr));
    }

    @Transactional
    public MaterialRequestResponse reject(
            Long id, MaterialRequestDecisionRequest decision, User decider) {
        MaterialRequest mr = findRequest(id);
        requirePending(mr);

        if (decision == null || decision.getDecisionNote() == null
                || decision.getDecisionNote().isBlank()) {
            throw new IllegalArgumentException("Red için gerekçe (decisionNote) zorunludur");
        }

        mr.setStatus(MaterialRequestStatus.REJECTED);
        mr.setDecidedBy(decider);
        mr.setDecidedAt(LocalDateTime.now());
        mr.setDecisionNote(decision.getDecisionNote());

        return mapToResponse(materialRequestRepository.save(mr));
    }

    private MaterialRequest findRequest(Long id) {
        return materialRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MaterialRequest", "id", id));
    }

    private void requirePending(MaterialRequest mr) {
        if (mr.getStatus() != MaterialRequestStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Sadece bekleyen talepler için karar verilebilir (mevcut durum: "
                            + mr.getStatus() + ")");
        }
    }

    private void ensureAccess(MaterialRequest request, User currentUser) {
        boolean isManager = currentUser.getRole().getName().name().equals("WAREHOUSE_MANAGER");
        boolean isOwner = request.getRequestedBy() != null
                && request.getRequestedBy().getId().equals(currentUser.getId());
        if (!isManager && !isOwner) {
            throw new AccessDeniedException("Bu talebi görüntüleme yetkiniz yok");
        }
    }

    private void attachItems(MaterialRequest mr, List<MaterialRequestItemRequest> itemRequests) {
        for (MaterialRequestItemRequest itemReq : itemRequests) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product", "id", itemReq.getProductId()));
            if (!product.isActive()) {
                throw new IllegalArgumentException(
                        "Pasif ürün talep edilemez: " + product.getName());
            }
            MaterialRequestItem item = MaterialRequestItem.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .build();
            mr.addItem(item);
        }
    }

    private MaterialRequestResponse mapToResponse(MaterialRequest mr) {
        List<MaterialRequestItemResponse> itemResponses = mr.getItems().stream()
                .map(this::mapItem)
                .toList();

        return MaterialRequestResponse.builder()
                .id(mr.getId())
                .status(mr.getStatus())
                .reason(mr.getReason())
                .createdAt(mr.getCreatedAt())
                .decidedAt(mr.getDecidedAt())
                .decisionNote(mr.getDecisionNote())
                .requestedBy(mapUser(mr.getRequestedBy()))
                .decidedBy(mapUser(mr.getDecidedBy()))
                .items(itemResponses)
                .build();
    }

    private MaterialRequestItemResponse mapItem(MaterialRequestItem item) {
        Product product = item.getProduct();
        return MaterialRequestItemResponse.builder()
                .id(item.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productBarcode(product.getBarcode())
                .currentStock(product.getCurrentStock())
                .quantity(item.getQuantity())
                .build();
    }

    private MaterialRequestResponse.UserRef mapUser(User user) {
        if (user == null) {
            return null;
        }
        return MaterialRequestResponse.UserRef.builder()
                .id(user.getId())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .department(user.getDepartment())
                .build();
    }
}
