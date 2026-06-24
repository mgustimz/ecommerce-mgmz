package com.example.ecommercemgmz.payment;

import com.example.ecommercemgmz.auth.AuthenticatedUser;
import com.example.ecommercemgmz.order.OrderResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/orders/{orderId}")
    PaymentResponse getCustomerPayment(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long orderId) {
        return paymentService.getCustomerPayment(user.id(), orderId);
    }

    @PostMapping("/admin/orders/{orderId}/simulate-paid")
    OrderResponse simulatePaid(@PathVariable Long orderId) {
        return paymentService.simulatePaid(orderId);
    }

    @PostMapping("/admin/orders/{orderId}/expire")
    PaymentResponse expirePayment(@PathVariable Long orderId) {
        return paymentService.expirePayment(orderId);
    }
}
