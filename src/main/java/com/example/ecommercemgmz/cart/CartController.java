package com.example.ecommercemgmz.cart;

import com.example.ecommercemgmz.auth.AuthenticatedUser;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    CartResponse findCart(@AuthenticationPrincipal AuthenticatedUser user) {
        return cartService.findCart(user.id());
    }

    @PostMapping("/items")
    CartResponse addItem(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody AddCartItemRequest request) {
        return cartService.addItem(user.id(), request);
    }

    @PutMapping("/items/{itemId}")
    CartResponse updateItem(@AuthenticationPrincipal AuthenticatedUser user,
                            @PathVariable Long itemId,
                            @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateItem(user.id(), itemId, request);
    }

    @DeleteMapping("/items/{itemId}")
    ResponseEntity<Void> removeItem(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long itemId) {
        cartService.removeItem(user.id(), itemId);
        return ResponseEntity.noContent().build();
    }
}
