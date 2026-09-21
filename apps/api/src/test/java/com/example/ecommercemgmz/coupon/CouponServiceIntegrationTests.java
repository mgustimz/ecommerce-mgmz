package com.example.ecommercemgmz.coupon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.ecommercemgmz.address.AddressRequest;
import com.example.ecommercemgmz.address.AddressService;
import com.example.ecommercemgmz.cart.AddCartItemRequest;
import com.example.ecommercemgmz.cart.CartService;
import com.example.ecommercemgmz.common.ApiException;
import com.example.ecommercemgmz.order.CheckoutRequest;
import com.example.ecommercemgmz.order.OrderResponse;
import com.example.ecommercemgmz.order.OrderService;
import com.example.ecommercemgmz.order.OrderStatus;
import com.example.ecommercemgmz.payment.PaymentMethod;
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

@SpringBootTest
@Transactional
class CouponServiceIntegrationTests {
    private final AppUserRepository userRepository;
    private final ProductService productService;
    private final AddressService addressService;
    private final CartService cartService;
    private final OrderService orderService;
    private final CouponService couponService;
    private final CouponRedemptionRepository redemptionRepository;

    @Autowired
    CouponServiceIntegrationTests(AppUserRepository userRepository,
                                  ProductService productService,
                                  AddressService addressService,
                                  CartService cartService,
                                  OrderService orderService,
                                  CouponService couponService,
                                  CouponRedemptionRepository redemptionRepository) {
        this.userRepository = userRepository;
        this.productService = productService;
        this.addressService = addressService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.couponService = couponService;
        this.redemptionRepository = redemptionRepository;
    }

    @Test
    void quoteComputesPercentAndFixedDiscounts() {
        Long customerId = createCustomer();
        createCoupon("PCT10", CouponDiscountType.PERCENT, "10", "0", null, null);
        createCoupon("FLAT50", CouponDiscountType.FIXED, "50000", "0", null, null);

        assertThat(couponService.quote("pct10", customerId, BigDecimal.valueOf(200000)).discountAmount())
                .isEqualByComparingTo("20000");
        assertThat(couponService.quote("FLAT50", customerId, BigDecimal.valueOf(200000)).discountAmount())
                .isEqualByComparingTo("50000");
        // A coupon never discounts more than the subtotal.
        assertThat(couponService.quote("FLAT50", customerId, BigDecimal.valueOf(30000)).discountAmount())
                .isEqualByComparingTo("30000");
    }

    @Test
    void quoteRejectsBelowMinimumSpend() {
        Long customerId = createCustomer();
        createCoupon("MINSPEND", CouponDiscountType.FIXED, "10000", "100000", null, null);

        assertThatThrownBy(() -> couponService.quote("MINSPEND", customerId, BigDecimal.valueOf(50000)))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(exception).hasMessage("Order does not meet the minimum spend for this coupon");
                });
    }

    @Test
    void quoteRejectsExpiredCoupons() {
        Long customerId = createCustomer();
        createCoupon("EXPIRED", CouponDiscountType.FIXED, "10000", "0", Instant.now().minusSeconds(10), null);

        assertThatThrownBy(() -> couponService.quote("EXPIRED", customerId, BigDecimal.valueOf(200000)))
                .isInstanceOfSatisfying(ApiException.class, exception -> {
                    assertThat(exception.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(exception).hasMessage("Coupon is not in its validity period");
                });
    }

    @Test
    void quoteRejectsInactiveCoupons() {
        Long customerId = createCustomer();
        couponService.create(new CouponInput(
                "INACTIVE",
                CouponDiscountType.FIXED,
                BigDecimal.TEN,
                BigDecimal.ZERO,
                Instant.now().minusSeconds(60),
                null,
                null,
                false
        ));

        assertThatThrownBy(() -> couponService.quote("INACTIVE", customerId, BigDecimal.valueOf(200000)))
                .hasMessage("Coupon is not active");
    }

    @Test
    void quoteRejectsUnknownCode() {
        Long customerId = createCustomer();

        assertThatThrownBy(() -> couponService.quote("NOPE", customerId, BigDecimal.valueOf(200000)))
                .hasMessage("Coupon code is not valid");
    }

    @Test
    void quoteRejectsWhenCapReached() {
        Long customerId = createCustomer();
        Long otherCustomer = createCustomer();
        createCoupon("ONCE", CouponDiscountType.FIXED, "10000", "0", null, 1);

        // One redemption quota consumed via checkout by the other customer.
        Long otherAddress = createAddress(otherCustomer);
        ProductResponse product = createProduct(10);
        cartService.addItem(otherCustomer, new AddCartItemRequest(product.id(), 1));
        orderService.checkout(otherCustomer, new CheckoutRequest(otherAddress, "REG", PaymentMethod.BANK_TRANSFER, "ONCE", null));

        assertThatThrownBy(() -> couponService.quote("ONCE", customerId, product.price()))
                .hasMessage("Coupon redemption limit has been reached");
    }

    @Test
    void quoteAndCheckoutRejectRepeatUseBySameCustomer() {
        Long customerId = createCustomer();
        Long addressId = createAddress(customerId);
        ProductResponse product = createProduct(10);
        // Unlimited global cap so the per-customer single-use rule is what triggers.
        createCoupon("ONCE", CouponDiscountType.FIXED, "10000", "0", null, null);

        cartService.addItem(customerId, new AddCartItemRequest(product.id(), 1));
        OrderResponse order = orderService.checkout(customerId, new CheckoutRequest(addressId, "REG", PaymentMethod.BANK_TRANSFER, "ONCE", null));
        assertThat(order.discountAmount()).isEqualByComparingTo("10000");

        assertThatThrownBy(() -> couponService.quote("ONCE", customerId, product.price()))
                .hasMessage("You have already used this coupon");
    }

    @Test
    void checkoutAppliesCouponDiscountAndRecordsRedemption() {
        Long customerId = createCustomer();
        Long addressId = createAddress(customerId);
        ProductResponse product = createProduct(10);
        createCoupon("SAVE20PCT", CouponDiscountType.PERCENT, "20", "0", null, null);

        cartService.addItem(customerId, new AddCartItemRequest(product.id(), 2));

        OrderResponse order = orderService.checkout(customerId, new CheckoutRequest(addressId, "REG", PaymentMethod.BANK_TRANSFER, "SAVE20PCT", null));

        assertThat(order.couponCode()).isEqualTo("SAVE20PCT");
        assertThat(order.discountAmount()).isEqualByComparingTo("40000");
        assertThat(order.status()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        // subtotal 200000 - 40000 discount + REG shipping 15000 (free-shipping threshold not met)
        assertThat(order.total()).isEqualByComparingTo(BigDecimal.valueOf(175000));
        assertThat(redemptionRepository.countByCouponId(findCouponId("SAVE20PCT"))).isEqualTo(1);
        assertThat(cartService.findCart(customerId).items()).isEmpty();
    }

    @Test
    void checkoutWithoutCouponIsBackwardCompatible() {
        Long customerId = createCustomer();
        Long addressId = createAddress(customerId);
        ProductResponse product = createProduct(10);
        cartService.addItem(customerId, new AddCartItemRequest(product.id(), 1));

        OrderResponse order = orderService.checkout(customerId, new CheckoutRequest(addressId, "REG", PaymentMethod.BANK_TRANSFER, null, null));

        assertThat(order.couponCode()).isNull();
        assertThat(order.discountAmount()).isEqualByComparingTo("0");
        // subtotal product.price() + REG shipping 15000 (free-shipping threshold not met)
        assertThat(order.total()).isEqualByComparingTo(product.price().add(BigDecimal.valueOf(15000)));
    }

    @Test
    void checkoutRejectsInvalidCouponCode() {
        Long customerId = createCustomer();
        Long addressId = createAddress(customerId);
        ProductResponse product = createProduct(10);
        cartService.addItem(customerId, new AddCartItemRequest(product.id(), 1));

        assertThatThrownBy(() -> orderService.checkout(customerId, new CheckoutRequest(addressId, "REG", PaymentMethod.BANK_TRANSFER, "BADCODE", null)))
                .hasMessage("Coupon code is not valid");
    }

    private Long findCouponId(String code) {
        return couponService.findAll().stream()
                .filter(coupon -> coupon.code().equalsIgnoreCase(code))
                .findFirst().orElseThrow().id();
    }

    private Long createCustomer() {
        String id = UUID.randomUUID().toString();
        return userRepository.save(new AppUser("Customer", "coupon-" + id + "@test.local", "passwordhash", UserRole.CUSTOMER)).getId();
    }

    private Long createAddress(Long customerId) {
        String id = UUID.randomUUID().toString();
        return addressService.create(customerId, new AddressRequest(
                "Home",
                "Customer",
                "08123456789",
                "Jl. Test No. " + id,
                "Jakarta",
                "DKI Jakarta",
                "11730",
                null,
                null,
                null,
                true
        )).id();
    }

    private void createCoupon(String code, CouponDiscountType type, String value, String minSubtotal, Instant validUntil, Integer maxRedemptions) {
        couponService.create(new CouponInput(
                code,
                type,
                BigDecimal.valueOf(Double.parseDouble(value)),
                BigDecimal.valueOf(Double.parseDouble(minSubtotal)),
                Instant.now().minusSeconds(60),
                validUntil,
                maxRedemptions,
                true
        ));
    }

    private ProductResponse createProduct(int stock) {
        String id = UUID.randomUUID().toString();
        return productService.create(new ProductRequest(
                "Product " + id,
                "product-" + id,
                "COUPON-SKU-" + id,
                "Test coupon product",
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
