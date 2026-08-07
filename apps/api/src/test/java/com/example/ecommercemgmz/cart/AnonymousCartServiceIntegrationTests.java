package com.example.ecommercemgmz.cart;

import static org.assertj.core.api.Assertions.assertThat;

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
class AnonymousCartServiceIntegrationTests {
    private final AppUserRepository userRepository;
    private final ProductService productService;
    private final AnonymousCartService anonymousCartService;
    private final CartService cartService;
    private final AnonymousCartRepository anonymousCartRepository;

    @Autowired
    AnonymousCartServiceIntegrationTests(AppUserRepository userRepository,
                                         ProductService productService,
                                         AnonymousCartService anonymousCartService,
                                         CartService cartService,
                                         AnonymousCartRepository anonymousCartRepository) {
        this.userRepository = userRepository;
        this.productService = productService;
        this.anonymousCartService = anonymousCartService;
        this.cartService = cartService;
        this.anonymousCartRepository = anonymousCartRepository;
    }

    @Test
    void repeatedAddsMergeIntoOneAnonymousItem() {
        UUID token = UUID.randomUUID();
        ProductResponse product = createProduct(10, ProductStatus.ACTIVE);

        anonymousCartService.addItem(token, new AddCartItemRequest(product.id(), 2));
        CartResponse cart = anonymousCartService.addItem(token, new AddCartItemRequest(product.id(), 3));

        assertThat(cart.items()).hasSize(1);
        assertThat(cart.items().get(0).quantity()).isEqualTo(5);
        assertThat(cart.subtotal()).isEqualByComparingTo("125000");
    }

    @Test
    void updateAndRemoveChangeAnonymousCartContents() {
        UUID token = UUID.randomUUID();
        ProductResponse product = createProduct(10, ProductStatus.ACTIVE);
        CartResponse cart = anonymousCartService.addItem(token, new AddCartItemRequest(product.id(), 2));
        Long itemId = cart.items().get(0).id();

        CartResponse updated = anonymousCartService.updateItem(token, itemId, new UpdateCartItemRequest(4));
        anonymousCartService.removeItem(token, itemId);

        assertThat(updated.items()).singleElement().extracting(CartItemResponse::quantity).isEqualTo(4);
        assertThat(anonymousCartService.get(token).items()).isEmpty();
    }

    @Test
    void mergeIntoCustomerCartSumsCollidingQuantities() {
        Long customerId = createCustomer();
        UUID token = UUID.randomUUID();
        ProductResponse product = createProduct(50, ProductStatus.ACTIVE);

        anonymousCartService.addItem(token, new AddCartItemRequest(product.id(), 2));
        cartService.addItem(customerId, new AddCartItemRequest(product.id(), 3));

        CartResponse merged = anonymousCartService.mergeInto(token, customerId);

        assertThat(merged.items()).hasSize(1);
        assertThat(merged.items().get(0).quantity()).isEqualTo(5);
        assertThat(anonymousCartRepository.findById(token)).isEmpty();
    }

    @Test
    void mergeWithEmptyAnonymousCartKeepsCustomerCartUntouched() {
        Long customerId = createCustomer();
        UUID token = UUID.randomUUID();
        ProductResponse product = createProduct(10, ProductStatus.ACTIVE);
        cartService.addItem(customerId, new AddCartItemRequest(product.id(), 3));

        CartResponse merged = anonymousCartService.mergeInto(token, customerId);

        assertThat(merged.items()).hasSize(1);
        assertThat(merged.items().get(0).quantity()).isEqualTo(3);
    }

    @Test
    void addItemRejectsArchivedProduct() {
        UUID token = UUID.randomUUID();
        ProductResponse product = createProduct(10, ProductStatus.ARCHIVED);

        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                        anonymousCartService.addItem(token, new AddCartItemRequest(product.id(), 1)))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(exception).hasMessage("Product is not active");
                });
    }

    private Long createCustomer() {
        String id = UUID.randomUUID().toString();
        return userRepository.save(new AppUser("Customer", "anon-customer-" + id + "@test.local", "password", UserRole.CUSTOMER)).getId();
    }

    private ProductResponse createProduct(int stock, ProductStatus status) {
        String id = UUID.randomUUID().toString();
        return productService.create(new ProductRequest(
                "Product " + id,
                "product-" + id,
                "ANON-SKU-" + id,
                "Test anonymous cart product",
                BigDecimal.valueOf(25000),
                null,
                stock,
                500,
                10,
                10,
                10,
                ProductShippingCategory.others,
                List.of("https://example.com/product.jpg"),
                null,
                status,
                null,
                0
        ));
    }
}
