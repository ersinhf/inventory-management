package com.smartinventory.product.service;

import com.smartinventory.category.entity.Category;
import com.smartinventory.category.service.CategoryService;
import com.smartinventory.common.exception.ResourceNotFoundException;
import com.smartinventory.product.dto.ProductRequest;
import com.smartinventory.product.dto.ProductResponse;
import com.smartinventory.product.entity.Product;
import com.smartinventory.product.repository.ProductRepository;
import com.smartinventory.supplier.entity.Supplier;
import com.smartinventory.supplier.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final CategoryService categoryService;

    public List<ProductResponse> getAllProducts(boolean activeOnly) {
        List<Product> products = activeOnly
                ? productRepository.findAllByActiveTrue()
                : productRepository.findAll();
        return products.stream().map(this::mapToResponse).toList();
    }

    public ProductResponse getProductById(Long id) {
        return mapToResponse(findProduct(id));
    }

    public ProductResponse getProductByBarcode(String barcode) {
        Product product = productRepository.findByBarcode(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "barcode", barcode));
        return mapToResponse(product);
    }

    public List<ProductResponse> getLowStockProducts() {
        return productRepository.findLowStockProducts().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsByBarcode(request.getBarcode())) {
            throw new IllegalArgumentException(
                    "Bu barkodla kayıtlı bir ürün zaten var: " + request.getBarcode());
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .barcode(request.getBarcode())
                .unitPrice(request.getUnitPrice())
                .minimumStockLevel(request.getMinimumStockLevel())
                .currentStock(0)
                .category(resolveCategory(request.getCategoryId()))
                .suppliers(resolveSuppliers(request.getSupplierIds()))
                .active(true)
                .build();

        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProduct(id);

        if (!product.getBarcode().equals(request.getBarcode())
                && productRepository.existsByBarcode(request.getBarcode())) {
            throw new IllegalArgumentException(
                    "Bu barkodla kayıtlı bir ürün zaten var: " + request.getBarcode());
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setBarcode(request.getBarcode());
        product.setUnitPrice(request.getUnitPrice());
        product.setMinimumStockLevel(request.getMinimumStockLevel());
        product.setCategory(resolveCategory(request.getCategoryId()));
        product.setSuppliers(resolveSuppliers(request.getSupplierIds()));

        return mapToResponse(productRepository.save(product));
    }

    @Transactional
    public void deactivateProduct(Long id) {
        Product product = findProduct(id);
        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional
    public void activateProduct(Long id) {
        Product product = findProduct(id);
        product.setActive(true);
        productRepository.save(product);
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    private Category resolveCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryService.findCategory(categoryId);
    }

    private Set<Supplier> resolveSuppliers(Set<Long> supplierIds) {
        if (supplierIds == null || supplierIds.isEmpty()) {
            return new HashSet<>();
        }
        List<Supplier> found = supplierRepository.findAllById(supplierIds);
        if (found.size() != supplierIds.size()) {
            Set<Long> foundIds = found.stream().map(Supplier::getId).collect(Collectors.toSet());
            Set<Long> missing = new HashSet<>(supplierIds);
            missing.removeAll(foundIds);
            throw new ResourceNotFoundException("Supplier", "id", missing.toString());
        }
        return new HashSet<>(found);
    }

    private ProductResponse mapToResponse(Product product) {
        ProductResponse.CategoryRef categoryRef = product.getCategory() == null
                ? null
                : ProductResponse.CategoryRef.builder()
                    .id(product.getCategory().getId())
                    .name(product.getCategory().getName())
                    .build();

        Set<ProductResponse.SupplierRef> supplierRefs = product.getSuppliers().stream()
                .map(s -> ProductResponse.SupplierRef.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .build())
                .collect(Collectors.toSet());

        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .barcode(product.getBarcode())
                .unitPrice(product.getUnitPrice())
                .currentStock(product.getCurrentStock())
                .minimumStockLevel(product.getMinimumStockLevel())
                .lowStock(product.isLowStock())
                .active(product.isActive())
                .category(categoryRef)
                .suppliers(supplierRefs)
                .build();
    }
}
