package com.example.ecommercemgmz.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.ecommercemgmz.address.AddressRequest;
import com.example.ecommercemgmz.address.AddressService;
import com.example.ecommercemgmz.cart.AddCartItemRequest;
import com.example.ecommercemgmz.cart.CartService;
import com.example.ecommercemgmz.common.ApiException;
import com.example.ecommercemgmz.inventory.InventoryMovementResponse;
import com.example.ecommercemgmz.inventory.InventoryService;
import com.example.ecommercemgmz.order.CheckoutRequest;
import com.example.ecommercemgmz.order.CustomerOrder;
import com.example.ecommercemgmz.order.CustomerOrderRepository;
import com.example.ecommercemgmz.order.OrderResponse;
import com.example.ecommercemgmz.order.OrderService;
import com.example.ecommercemgmz.order.OrderStatus;
import com.example.ecommercemgmz.payment.PaymentMethod;
import com.example.ecommercemgmz.payment.PaymentStatus;
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
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@SpringBootTest
@Transactional
class PaymentExpiryIntegrationTests {
    private final AppUserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final AddressService addressService;
    private final CartService cartService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final InventoryService inventoryService;
    private final CustomerOrderRepository orderRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    PaymentExpiryIntegrationTests(AppUserRepository userRepository,
                                  ProductRepository productRepository,
                                  ProductService productService,
                                  AddressService addressService,
                                  CartService cartService,
                                  OrderService orderService,
                                  PaymentService paymentService,
                                  InventoryService inventoryService,
                                  CustomerOrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.addressService = addressService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.inventoryService = inventoryService;
        this.orderRepository = orderRepository;
    }

    @Test
    void expirePaymentReleasesStockAndCancelsOrder() {
        Long customerId = createCustomer();
        createCustomer();
        Long addressId = createAddress(customerId);
        ProductResponse product = createProduct(10);
        cartService.addItem(customerId, new AddCartItemRequest(product.id(), 3));
        OrderResponse order = checkout(customerId, addressId);
        forceExpiry(order.id());

        PaymentResponse payment = paymentService.expirePayment(order.id());

        Product released = productRepository.findById(product.id()).orElseThrow();
        CustomerOrder cancelled = orderRepository.findById(order.id()).orElseThrow();
        assertThat(payment.status()).isEqualTo(PaymentStatus.FAILED);
        assertThat(cancelled.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(cancelled.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(released.getStock()).isEqualTo(10);
        assertThat(cancelled.getCancellationReason()).isEqualTo("Payment expired");

        List<InventoryMovementResponse> movements = inventoryService.findRecentMovements(product.id());
        assertThat(movements).first().satisfies(movement -> {
            assertThat(movement.type()).isEqualTo(com.example.ecommercemgmz.inventory.InventoryMovementType.ORDER_CANCELLED);
            assertThat(movement.quantityChange()).isEqualTo(3);
            assertThat(movement.reason()).isEqualTo("Stock released after payment expiry");
        });
    }

    @Test
    void simulatePaidOnExpiredOrderReleasesStockAndIsRejected() {
        Long customerId = createCustomer();
        Long addressId = createAddress(customerId);
        ProductResponse product = createProduct(10);
        cartService.addItem(customerId, new AddCartItemRequest(product.id(), 2));
        OrderResponse order = checkout(customerId, addressId);
        forceExpiry(order.id());

        assertThatThrownBy(() -> paymentService.simulatePaid(order.id()))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(exception).hasMessage("Payment has expired");
                });

        Product released = productRepository.findById(product.id()).orElseThrow();
        CustomerOrder expiredOrder = orderRepository.findById(order.id()).orElseThrow();
        assertThat(released.getStock()).isEqualTo(10);
        assertThat(expiredOrder.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(expiredOrder.getPaymentStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void expireStaleOrdersReleasesOnlyExpiredPendingOrders() {
        Long customerId = createCustomer();
        Long addressId = createAddress(customerId);
        ProductResponse product = createProduct(10);
        cartService.addItem(customerId, new AddCartItemRequest(product.id(), 2));
        OrderResponse expiredOrder = checkout(customerId, addressId);
        forceExpiry(expiredOrder.id());

        cartService.addItem(customerId, new AddCartItemRequest(product.id(), 1));
        OrderResponse liveOrder = checkout(customerId, addressId); // not yet expired

        int releasedCount = paymentService.expireStaleOrders();

        assertThat(releasedCount).isEqualTo(1);
        assertThat(orderRepository.findById(expiredOrder.id()).orElseThrow().getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(orderRepository.findById(liveOrder.id()).orElseThrow().getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        // stock: 10 - 2 (expired) = 8, then +2 back = 10, minus 1 live order = 9
        assertThat(productRepository.findById(product.id()).orElseThrow().getStock()).isEqualTo(9);
    }

    @Test
    void deductStockRefusesWhenRequestedQuantityExceedsStock() {
        ProductResponse product = createProduct(2);

        int failed = productRepository.deductStock(product.id(), 5);
        int success = productRepository.deductStock(product.id(), 2);

        // JPQL bulk updates bypass the persistence context; refresh before reading.
        entityManager.flush();
        entityManager.clear();

        assertThat(failed).isZero();
        assertThat(success).isEqualTo(1);
        assertThat(productRepository.findById(product.id()).orElseThrow().getStock()).isZero();
    }

    private OrderResponse checkout(Long customerId, Long addressId) {
        return orderService.checkout(customerId, new CheckoutRequest(addressId, "REG", PaymentMethod.BANK_TRANSFER, null));
    }

    private void forceExpiry(Long orderId) {
        CustomerOrder order = orderRepository.findById(orderId).orElseThrow();
        order.setPaymentExpiresAt(Instant.now().minusSeconds(120));
        orderRepository.save(order);
    }

    private Long createCustomer() {
        String id = UUID.randomUUID().toString();
        return userRepository.save(new AppUser("Customer", "payment-expiry-" + id + "@test.local", "password", UserRole.CUSTOMER)).getId();
    }

    private Long createAddress(Long customerId) {
        String id = UUID.randomUUID().toString();
        return addressService.create(customerId, new AddressRequest(
                "Home",
                "Customer",
                "08123456789",
                "Jl. Test No. 1",
                "Jakarta",
                "DKI Jakarta",
                "11730",
                null,
                null,
                null,
                true
        )).id();
    }

    private ProductResponse createProduct(int stock) {
        String id = UUID.randomUUID().toString();
        return productService.create(new ProductRequest(
                "Product " + id,
                "product-" + id,
                "PAY-EXP-SKU-" + id,
                "Test payment expiry product",
                BigDecimal.valueOf(100000),
                null,
                stock,
                500,
                10,
                10,
                10,
                ProductShippingCategory.others,
                List.of("https://example.com/product.jpg"),
                null,
                ProductStatus.ACTIVE,
                null,
                0
        ));
    }
}
