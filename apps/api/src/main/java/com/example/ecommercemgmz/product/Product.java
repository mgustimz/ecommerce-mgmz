package com.example.ecommercemgmz.product;

import com.example.ecommercemgmz.category.Category;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(precision = 19, scale = 2)
    private BigDecimal originalPrice;

    @Column(precision = 2, scale = 1, nullable = false)
    private BigDecimal averageRating = BigDecimal.ZERO;

    @Column(nullable = false)
    private int reviewCount;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    private int weightGram;

    @Column(nullable = false)
    private int lengthCm;

    @Column(nullable = false)
    private int widthCm;

    @Column(nullable = false)
    private int heightCm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductShippingCategory shippingCategory = ProductShippingCategory.others;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductStatus status = ProductStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    public Product(String name, String slug, String sku, String description, BigDecimal price, BigDecimal originalPrice,
                   int stock, int weightGram, int lengthCm, int widthCm, int heightCm, ProductShippingCategory shippingCategory,
                   ProductStatus status, Category category) {
        this.name = name;
        this.slug = slug;
        this.sku = sku;
        this.description = description;
        this.price = price;
        this.originalPrice = originalPrice;
        this.stock = stock;
        this.weightGram = weightGram;
        this.lengthCm = lengthCm;
        this.widthCm = widthCm;
        this.heightCm = heightCm;
        this.shippingCategory = shippingCategory;
        this.status = status;
        this.category = category;
    }

    public void replaceImages(List<String> imageUrls) {
        images.clear();
        for (int index = 0; index < imageUrls.size(); index++) {
            ProductImage image = new ProductImage(imageUrls.get(index), index);
            image.setProduct(this);
            images.add(image);
        }
    }

    public boolean isPublished() {
        return status == ProductStatus.ACTIVE;
    }
}
