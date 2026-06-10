package com.example.ecommercemgmz.order;

import com.example.ecommercemgmz.auth.AuthenticatedUser;
import com.example.ecommercemgmz.user.UserRole;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders/checkout")
    ResponseEntity<OrderResponse> checkout(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody CheckoutRequest request) {
        OrderResponse response = orderService.checkout(user.id(), request);
        return ResponseEntity.created(URI.create("/api/orders/" + response.id())).body(response);
    }

    @GetMapping("/orders")
    List<OrderResponse> findCustomerOrders(@AuthenticationPrincipal AuthenticatedUser user) {
        return orderService.findCustomerOrders(user.id());
    }

    @GetMapping("/orders/{id}")
    OrderResponse findOrder(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        if (user.role() == UserRole.ADMIN) {
            return orderService.findOrder(id);
        }
        return orderService.findCustomerOrder(user.id(), id);
    }

    @PostMapping("/orders/{id}/cancel")
    OrderResponse cancelCustomerOrder(@AuthenticationPrincipal AuthenticatedUser user,
                                      @PathVariable Long id,
                                      @Valid @RequestBody CancelOrderRequest request) {
        return orderService.cancelCustomerOrder(user.id(), id, request);
    }

    @GetMapping("/admin/orders")
    List<OrderResponse> findAllOrders() {
        return orderService.findAllOrders();
    }

    @PutMapping("/admin/orders/{id}/status")
    OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateStatus(id, request);
    }

    @PostMapping("/admin/orders/{id}/cancel")
    OrderResponse cancelAdminOrder(@PathVariable Long id, @Valid @RequestBody CancelOrderRequest request) {
        return orderService.cancelAdminOrder(id, request);
    }
}
