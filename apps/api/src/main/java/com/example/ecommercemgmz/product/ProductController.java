package com.example.ecommercemgmz.product;

import com.example.ecommercemgmz.common.PageResponse;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    PageResponse<ProductResponse> findActiveProducts(
            @RequestParam(required = false, name = "q") String query,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "NAME_ASC") ProductSort sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return productService.findActiveProducts(query, categoryId, sort, Math.max(page, 0), Math.min(Math.max(size, 1), 100));
    }

    @GetMapping("/products/{id}")
    ProductResponse findActiveProduct(@PathVariable Long id) {
        return productService.findActiveProduct(id);
    }

    @GetMapping("/products/slug/{slug}")
    ProductResponse findActiveProductBySlug(@PathVariable String slug) {
        return productService.findActiveProductBySlug(slug);
    }

    @PostMapping("/admin/products")
    ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        ProductResponse response = productService.create(request);
        return ResponseEntity.created(URI.create("/api/products/" + response.id())).body(response);
    }

    @PutMapping("/admin/products/{id}")
    ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/admin/products/{id}")
    ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
