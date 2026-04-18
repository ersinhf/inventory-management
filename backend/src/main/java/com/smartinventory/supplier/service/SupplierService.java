package com.smartinventory.supplier.service;

import com.smartinventory.common.exception.ResourceNotFoundException;
import com.smartinventory.supplier.dto.SupplierRequest;
import com.smartinventory.supplier.dto.SupplierResponse;
import com.smartinventory.supplier.entity.Supplier;
import com.smartinventory.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public List<SupplierResponse> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<SupplierResponse> getActiveSuppliers() {
        return supplierRepository.findAllByActiveTrue().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public SupplierResponse getSupplierById(Long id) {
        return mapToResponse(findSupplier(id));
    }

    @Transactional
    public SupplierResponse createSupplier(SupplierRequest request) {
        validateUniqueness(request, null);

        Supplier supplier = Supplier.builder()
                .name(request.getName())
                .contactPerson(request.getContactPerson())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .taxNumber(request.getTaxNumber())
                .active(true)
                .build();

        return mapToResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierResponse updateSupplier(Long id, SupplierRequest request) {
        Supplier supplier = findSupplier(id);
        validateUniqueness(request, supplier);

        supplier.setName(request.getName());
        supplier.setContactPerson(request.getContactPerson());
        supplier.setEmail(request.getEmail());
        supplier.setPhone(request.getPhone());
        supplier.setAddress(request.getAddress());
        supplier.setTaxNumber(request.getTaxNumber());

        return mapToResponse(supplierRepository.save(supplier));
    }

    @Transactional
    public void deactivateSupplier(Long id) {
        Supplier supplier = findSupplier(id);
        supplier.setActive(false);
        supplierRepository.save(supplier);
    }

    @Transactional
    public void activateSupplier(Long id) {
        Supplier supplier = findSupplier(id);
        supplier.setActive(true);
        supplierRepository.save(supplier);
    }

    private Supplier findSupplier(Long id) {
        return supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
    }

    private void validateUniqueness(SupplierRequest request, Supplier existing) {
        String newTaxNumber = request.getTaxNumber();
        String newEmail = request.getEmail();

        if (newTaxNumber != null && !newTaxNumber.isBlank()) {
            boolean taxChanged = existing == null || !newTaxNumber.equals(existing.getTaxNumber());
            if (taxChanged && supplierRepository.existsByTaxNumber(newTaxNumber)) {
                throw new IllegalArgumentException(
                        "Bu vergi numarasıyla kayıtlı bir tedarikçi zaten var: " + newTaxNumber);
            }
        }

        if (newEmail != null && !newEmail.isBlank()) {
            boolean emailChanged = existing == null || !newEmail.equals(existing.getEmail());
            if (emailChanged && supplierRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException(
                        "Bu e-posta adresiyle kayıtlı bir tedarikçi zaten var: " + newEmail);
            }
        }
    }

    private SupplierResponse mapToResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .id(supplier.getId())
                .name(supplier.getName())
                .contactPerson(supplier.getContactPerson())
                .email(supplier.getEmail())
                .phone(supplier.getPhone())
                .address(supplier.getAddress())
                .taxNumber(supplier.getTaxNumber())
                .active(supplier.isActive())
                .build();
    }
}
