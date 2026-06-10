package com.example.ecommercemgmz.cart;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.ecommercemgmz.common.ApiException;
import com.example.ecommercemgmz.product.ProductRequest;
import com.example.ecommercemgmz.product.ProductResponse;
import com.example.ecommercemgmz.product.ProductService;
import com.example.ecommercemgmz.product.ProductShippingCategory;
import com.example.ecommercemgmz.product.ProductStatus;
import com.example.ecommercemgmz.user.AppUser;
import com.example.ecommercemgmz.user.AppUserRepository;
import com.example.ecommercemgmz.user.UserRole;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class CartServiceIntegrationTests {
    private final AppUserRepository userRepository;
    private final ProductService productService;
    private final CartService cartService;

    @Autowired
    CartServiceIntegrationTests(AppUserRepository userRepository, ProductService productService, CartService cartService) {
        this.userRepository = userRepository;
        this.productService = productService;
        this.cartService = cartService;
    }

    @Test
    void repeatedAddsMergeIntoOneCartItemAndUpdateSubtotal() {
        Long customerId = createCustomer();
        ProductResponse product = createProduct(10, ProductStatus.ACTIVE);

        cartService.addItem(customerId, new AddCartItemRequest(product.id(), 2));
        CartResponse cart = cartService.addItem(customerId, new AddCartItemRequest(product.id(), 3));

        assertThat(cart.items()).hasSize(1);
        assertThat(cart.items().get(0).quantity()).isEqualTo(5);
        assertThat(cart.subtotal()).isEqualByComparingTo("125000");
    }

    @Test
    void updateAndRemoveItemChangesCartContents() {
        Long customerId = createCustomer();
        ProductResponse product = createProduct(10, ProductStatus.ACTIVE);
        CartResponse cart = cartService.addItem(customerId, new AddCartItemRequest(product.id(), 2));
        Long itemId = cart.items().get(0).id();

        CartResponse updatedCart = cartService.updateItem(customerId, itemId, new UpdateCartItemRequest(4));
        cartService.removeItem(customerId, itemId);

        assertThat(updatedCart.items()).singleElement().extracting(CartItemResponse::quantity).isEqualTo(4);
        assertThat(cartService.findCart(customerId).items()).isEmpty();
    }

    @Test
    void addItemRejectsQuantityAboveStock() {
        Long customerId = createCustomer();
        ProductResponse product = createProduct(2, ProductStatus.ACTIVE);

        assertThatThrownBy(() -> cartService.addItem(customerId, new AddCartItemRequest(product.id(), 3)))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(exception).hasMessageStartingWith("Insufficient stock for Product");
                });
    }

    @Test
    void addItemRejectsArchivedProduct() {
        Long customerId = createCustomer();
        ProductResponse product = createProduct(10, ProductStatus.ARCHIVED);

        assertThatThrownBy(() -> cartService.addItem(customerId, new AddCartItemRequest(product.id(), 1)))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(exception).hasMessage("Product is not active");
                });
    }

    private Long createCustomer() {
        String id = UUID.randomUUID().toString();
        return userRepository.save(new AppUser("Customer", "cart-customer-" + id + "@test.local", "password", UserRole.CUSTOMER)).getId();
    }

    private ProductResponse createProduct(int stock, ProductStatus status) {
        String id = UUID.randomUUID().toString();
        return productService.create(new ProductRequest(
                "Product " + id,
                "product-" + id,
                "CART-SKU-" + id,
                "Test cart product",
                BigDecimal.valueOf(25000),
                stock,
                500,
                10,
                10,
                10,
                ProductShippingCategory.others,
                List.of("https://example.com/product.jpg"),
                null,
                status
        ));
    }
}
