package com.example.ecommercemgmz.product;

import com.example.ecommercemgmz.category.Category;
import com.example.ecommercemgmz.category.CategoryService;
import com.example.ecommercemgmz.common.ApiException;
import com.example.ecommercemgmz.common.PageResponse;
import com.example.ecommercemgmz.inventory.InventoryMovementType;
import com.example.ecommercemgmz.inventory.InventoryService;
import java.util.List;
import java.util.Locale;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final InventoryService inventoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService, InventoryService inventoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.inventoryService = inventoryService;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> findActiveProducts(String query, Long categoryId, ProductSort sort, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, toSort(sort));
        Specification<Product> specification = activeProducts(query, categoryId);
        return PageResponse.from(productRepository.findAll(specification, pageable).map(ProductResponse::from));
    }

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> findAllProducts(String query, Long categoryId, ProductSort sort, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, toSort(sort));
        Specification<Product> specification = products(query, categoryId);
        return PageResponse.from(productRepository.findAll(specification, pageable).map(ProductResponse::from));
    }

    @Transactional(readOnly = true)
    public ProductResponse findActiveProduct(Long id) {
        Product product = findEntity(id);
        if (!product.isPublished()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Product not found");
        }
        return ProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse findProduct(Long id) {
        return ProductResponse.from(findEntity(id));
    }

    @Transactional(readOnly = true)
    public ProductResponse findActiveProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
        if (!product.isPublished()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Product not found");
        }
        return ProductResponse.from(product);
    }

    public Product findEntity(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Category category = request.categoryId() == null ? null : categoryService.findEntity(request.categoryId());
        Product product = new Product(
                request.name(),
                resolveSlug(request),
                request.sku(),
                request.description(),
                request.price(),
                request.stock(),
                request.weightGram(),
                request.lengthCm(),
                request.widthCm(),
                request.heightCm(),
                request.shippingCategory() == null ? ProductShippingCategory.others : request.shippingCategory(),
                request.status(),
                category
        );
        product.replaceImages(cleanImageUrls(request.imageUrls()));
        Product savedProduct = productRepository.save(product);
        inventoryService.record(savedProduct, null, InventoryMovementType.PRODUCT_CREATED, savedProduct.getStock(), "Initial product stock");
        return ProductResponse.from(savedProduct);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = findEntity(id);
        int previousStock = product.getStock();
        Category category = request.categoryId() == null ? null : categoryService.findEntity(request.categoryId());
        product.setName(request.name());
        product.setSlug(resolveSlug(request));
        product.setSku(request.sku());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setWeightGram(request.weightGram());
        product.setLengthCm(request.lengthCm());
        product.setWidthCm(request.widthCm());
        product.setHeightCm(request.heightCm());
        product.setShippingCategory(request.shippingCategory() == null ? ProductShippingCategory.others : request.shippingCategory());
        product.setCategory(category);
        product.setStatus(request.status());
        product.replaceImages(cleanImageUrls(request.imageUrls()));
        inventoryService.record(product, null, InventoryMovementType.ADMIN_ADJUSTMENT, product.getStock() - previousStock, "Admin product stock update");
        return ProductResponse.from(product);
    }

    @Transactional
    public void delete(Long id) {
        Product product = findEntity(id);
        product.setStatus(ProductStatus.ARCHIVED);
    }

    private Specification<Product> activeProducts(String query, Long categoryId) {
        return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("status"), ProductStatus.ACTIVE),
                products(query, categoryId).toPredicate(root, criteriaQuery, criteriaBuilder)
        );
    }

    private Specification<Product> products(String query, Long categoryId) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            var predicate = criteriaBuilder.conjunction();
            if (query != null && !query.isBlank()) {
                String keyword = "%" + query.toLowerCase() + "%";
                predicate = criteriaBuilder.and(
                        predicate,
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), keyword)
                );
            }
            if (categoryId != null) {
                predicate = criteriaBuilder.and(predicate, criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }
            return predicate;
        };
    }

    private Sort toSort(ProductSort sort) {
        ProductSort selectedSort = sort == null ? ProductSort.NAME_ASC : sort;
        return switch (selectedSort) {
            case NEWEST -> Sort.by(Sort.Direction.DESC, "id");
            case PRICE_ASC -> Sort.by(Sort.Direction.ASC, "price").and(Sort.by(Sort.Direction.ASC, "name"));
            case PRICE_DESC -> Sort.by(Sort.Direction.DESC, "price").and(Sort.by(Sort.Direction.ASC, "name"));
            case NAME_ASC -> Sort.by(Sort.Direction.ASC, "name");
        };
    }

    private String resolveSlug(ProductRequest request) {
        if (request.slug() != null && !request.slug().isBlank()) {
            return normalizeSlug(request.slug());
        }
        return normalizeSlug(request.name());
    }

    private String normalizeSlug(String value) {
        String slug = value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-)|(-$)", "");
        if (slug.isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Product slug is invalid");
        }
        return slug;
    }

    private List<String> cleanImageUrls(List<String> imageUrls) {
        if (imageUrls == null) {
            return List.of();
        }
        return imageUrls.stream()
                .filter(url -> url != null && !url.isBlank())
                .map(String::trim)
                .toList();
    }
}
