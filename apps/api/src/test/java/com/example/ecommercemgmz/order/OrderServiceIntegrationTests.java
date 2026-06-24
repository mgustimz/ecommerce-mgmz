package com.example.ecommercemgmz.order;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.ecommercemgmz.address.AddressRequest;
import com.example.ecommercemgmz.address.AddressService;
import com.example.ecommercemgmz.cart.AddCartItemRequest;
import com.example.ecommercemgmz.cart.CartService;
import com.example.ecommercemgmz.payment.PaymentMethod;
import com.example.ecommercemgmz.product.Product;
import com.example.ecommercemgmz.product.ProductRepository;
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
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class OrderServiceIntegrationTests {
    private final AppUserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final AddressService addressService;
    private final CartService cartService;
    private final OrderService orderService;

    @Autowired
    OrderServiceIntegrationTests(AppUserRepository userRepository,
                                 ProductRepository productRepository,
                                 ProductService productService,
                                 AddressService addressService,
                                 CartService cartService,
                                 OrderService orderService) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.addressService = addressService;
        this.cartService = cartService;
        this.orderService = orderService;
    }

    @Test
    void checkoutDeductsProductStock() {
        Long customerId = createCustomer();
        ProductResponse product = createProduct(10);
        Long addressId = createAddress(customerId);

        cartService.addItem(customerId, new AddCartItemRequest(product.id(), 3));

        OrderResponse order = orderService.checkout(customerId, new CheckoutRequest(addressId, "REG", PaymentMethod.QRIS, "test order"));

        Product updatedProduct = productRepository.findById(product.id()).orElseThrow();
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(order.paymentMethod()).isEqualTo(PaymentMethod.QRIS);
        assertThat(order.shippingFee()).isEqualByComparingTo("0");
        assertThat(updatedProduct.getStock()).isEqualTo(7);
        assertThat(cartService.findCart(customerId).items()).isEmpty();
    }

    @Test
    void cancellingPendingOrderRestoresProductStock() {
        Long customerId = createCustomer();
        ProductResponse product = createProduct(10);
        Long addressId = createAddress(customerId);
        cartService.addItem(customerId, new AddCartItemRequest(product.id(), 4));
        OrderResponse order = orderService.checkout(customerId, new CheckoutRequest(addressId, "REG", PaymentMethod.BANK_TRANSFER, null));

        OrderResponse cancelledOrder = orderService.cancelCustomerOrder(customerId, order.id(), new CancelOrderRequest("customer request"));

        Product updatedProduct = productRepository.findById(product.id()).orElseThrow();
        assertThat(cancelledOrder.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(cancelledOrder.cancellationReason()).isEqualTo("customer request");
        assertThat(updatedProduct.getStock()).isEqualTo(10);
    }

    private Long createCustomer() {
        String id = UUID.randomUUID().toString();
        return userRepository.save(new AppUser("Customer", "customer-" + id + "@test.local", "password", UserRole.CUSTOMER)).getId();
    }

    private ProductResponse createProduct(int stock) {
        String id = UUID.randomUUID().toString();
        return productService.create(new ProductRequest(
                "Product " + id,
                "product-" + id,
                "SKU-" + id,
                "Test product",
                BigDecimal.valueOf(100000),
                stock,
                500,
                10,
                10,
                10,
                ProductShippingCategory.others,
                List.of("https://example.com/product.jpg"),
                null,
                ProductStatus.ACTIVE
        ));
    }

    private Long createAddress(Long customerId) {
        return addressService.create(customerId, new AddressRequest(
                "Home",
                "Customer",
                "08123456789",
                "Jl. Test No. 1",
                "Jakarta Selatan",
                "DKI Jakarta",
                "12250",
                null,
                null,
                null,
                true
        )).id();
    }
}
