package com.example.ecommercemgmz.product;

import com.example.ecommercemgmz.category.Category;
import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
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

    protected Product() {
    }

    public Product(String name, String slug, String sku, String description, BigDecimal price, int stock, int weightGram, int lengthCm, int widthCm, int heightCm, ProductShippingCategory shippingCategory, ProductStatus status, Category category) {
        this.name = name;
        this.slug = slug;
        this.sku = sku;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.weightGram = weightGram;
        this.lengthCm = lengthCm;
        this.widthCm = widthCm;
        this.heightCm = heightCm;
        this.shippingCategory = shippingCategory;
        this.status = status;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getWeightGram() {
        return weightGram;
    }

    public void setWeightGram(int weightGram) {
        this.weightGram = weightGram;
    }

    public int getLengthCm() {
        return lengthCm;
    }

    public void setLengthCm(int lengthCm) {
        this.lengthCm = lengthCm;
    }

    public int getWidthCm() {
        return widthCm;
    }

    public void setWidthCm(int widthCm) {
        this.widthCm = widthCm;
    }

    public int getHeightCm() {
        return heightCm;
    }

    public void setHeightCm(int heightCm) {
        this.heightCm = heightCm;
    }

    public ProductShippingCategory getShippingCategory() {
        return shippingCategory;
    }

    public void setShippingCategory(ProductShippingCategory shippingCategory) {
        this.shippingCategory = shippingCategory;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<ProductImage> getImages() {
        return images;
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
