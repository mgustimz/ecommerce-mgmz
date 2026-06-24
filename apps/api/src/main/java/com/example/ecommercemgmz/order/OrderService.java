package com.example.ecommercemgmz.order;

import com.example.ecommercemgmz.address.AddressService;
import com.example.ecommercemgmz.address.CustomerAddress;
import com.example.ecommercemgmz.cart.CartItem;
import com.example.ecommercemgmz.cart.CartService;
import com.example.ecommercemgmz.common.ApiException;
import com.example.ecommercemgmz.inventory.InventoryMovementType;
import com.example.ecommercemgmz.inventory.InventoryService;
import com.example.ecommercemgmz.product.Product;
import com.example.ecommercemgmz.product.ProductService;
import com.example.ecommercemgmz.shipping.ShippingRateResponse;
import com.example.ecommercemgmz.shipping.ShippingService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private final CustomerOrderRepository orderRepository;
    private final CartService cartService;
    private final AddressService addressService;
    private final ShippingService shippingService;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final long paymentExpirationMinutes;

    public OrderService(CustomerOrderRepository orderRepository, CartService cartService, AddressService addressService, ShippingService shippingService, ProductService productService, InventoryService inventoryService, @Value("${app.payment.expiration-minutes}") long paymentExpirationMinutes) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.addressService = addressService;
        this.shippingService = shippingService;
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.paymentExpirationMinutes = paymentExpirationMinutes;
    }

    @Transactional
    public OrderResponse checkout(Long customerId, CheckoutRequest request) {
        List<CartItem> cartItems = cartService.findItemsForCheckout(customerId);
        if (cartItems.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }
        CustomerAddress address = addressService.findEntity(customerId, request.addressId());

        BigDecimal subtotal = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        ShippingRateResponse shippingRate = shippingService.findRate(request.shippingServiceCode(), address, cartItems, subtotal);
        BigDecimal shippingFee = shippingRate.fee();
        CustomerOrder order = new CustomerOrder(
                customerId,
                subtotal,
                shippingFee,
                subtotal.add(shippingFee),
                address.formatForOrder(),
                request.notes(),
                request.paymentMethod(),
                generatePaymentReference(),
                Instant.now().plusSeconds(paymentExpirationMinutes * 60),
                shippingRate.serviceCode(),
                shippingRate.courierName() + " " + shippingRate.serviceName()
        );

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (!product.isPublished()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Product is not active: " + product.getName());
            }
            if (product.getStock() < cartItem.getQuantity()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Insufficient stock for " + product.getName());
            }
            product.setStock(product.getStock() - cartItem.getQuantity());

            BigDecimal lineTotal = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            order.addItem(new OrderItem(product.getId(), product.getName(), product.getPrice(), cartItem.getQuantity(), lineTotal));
        }

        CustomerOrder savedOrder = orderRepository.save(order);
        for (CartItem cartItem : cartItems) {
            inventoryService.record(
                    cartItem.getProduct(),
                    savedOrder.getId(),
                    InventoryMovementType.ORDER_CREATED,
                    -cartItem.getQuantity(),
                    "Stock deducted for order"
            );
        }
        cartService.clear(customerId);
        return OrderResponse.from(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findCustomerOrders(Long customerId) {
        return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream().map(OrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findAllOrders() {
        return orderRepository.findAll().stream().map(OrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findOrder(Long id) {
        return OrderResponse.from(findEntity(id));
    }

    @Transactional(readOnly = true)
    public OrderResponse findCustomerOrder(Long customerId, Long id) {
        CustomerOrder order = findEntity(id);
        if (!order.getCustomerId().equals(customerId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Order not found");
        }
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, UpdateOrderStatusRequest request) {
        CustomerOrder order = findEntity(id);
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cancelled order status cannot be changed");
        }
        order.setStatus(request.status());
        return OrderResponse.from(order);
    }

    @Transactional
    public OrderResponse cancelCustomerOrder(Long customerId, Long id, CancelOrderRequest request) {
        CustomerOrder order = findEntity(id);
        if (!order.getCustomerId().equals(customerId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Order not found");
        }
        if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Customer can only cancel orders pending payment");
        }
        return cancel(order, request.reason());
    }

    @Transactional
    public OrderResponse cancelAdminOrder(Long id, CancelOrderRequest request) {
        CustomerOrder order = findEntity(id);
        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.COMPLETED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Shipped or completed orders cannot be cancelled");
        }
        return cancel(order, request.reason());
    }

    private OrderResponse cancel(CustomerOrder order, String reason) {
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Order is already cancelled");
        }
        restoreStock(order);
        order.cancel(reason);
        return OrderResponse.from(order);
    }

    private void restoreStock(CustomerOrder order) {
        for (OrderItem item : order.getItems()) {
            Product product = productService.findEntity(item.getProductId());
            product.setStock(product.getStock() + item.getQuantity());
            inventoryService.record(product, order.getId(), InventoryMovementType.ORDER_CANCELLED, item.getQuantity(), "Stock restored after order cancellation");
        }
    }

    private CustomerOrder findEntity(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Order not found"));
    }

    private String generatePaymentReference() {
        return "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}
