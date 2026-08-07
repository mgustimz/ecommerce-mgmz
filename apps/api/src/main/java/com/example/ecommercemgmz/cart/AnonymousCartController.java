package com.example.ecommercemgmz.cart;

import com.example.ecommercemgmz.auth.AuthenticatedUser;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/cart/anonymous")
@RequiredArgsConstructor
public class AnonymousCartController {
    private final AnonymousCartService anonymousCartService;
    private final AnonymousCartCookieSupport cookieSupport;

    @GetMapping
    CartResponse get(@RequestHeader(value = "X-Anonymous-Cart-Token", required = false) String headerToken,
                     HttpServletResponse response) {
        UUID token = resolveToken(headerToken, response);
        return anonymousCartService.get(token);
    }

    @PostMapping("/items")
    CartResponse addItem(@RequestHeader(value = "X-Anonymous-Cart-Token", required = false) String headerToken,
                         HttpServletResponse response,
                         @Valid @RequestBody AddCartItemRequest request) {
        UUID token = resolveToken(headerToken, response);
        return anonymousCartService.addItem(token, request);
    }

    @PutMapping("/items/{itemId}")
    CartResponse updateItem(@RequestHeader(value = "X-Anonymous-Cart-Token", required = false) String headerToken,
                            HttpServletResponse response,
                            @PathVariable Long itemId,
                            @Valid @RequestBody UpdateCartItemRequest request) {
        UUID token = resolveToken(headerToken, response);
        return anonymousCartService.updateItem(token, itemId, request);
    }

    @DeleteMapping("/items/{itemId}")
    ResponseEntity<Void> removeItem(@RequestHeader(value = "X-Anonymous-Cart-Token", required = false) String headerToken,
                                    HttpServletResponse response,
                                    @PathVariable Long itemId) {
        UUID token = resolveToken(headerToken, response);
        anonymousCartService.removeItem(token, itemId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/merge")
    CartResponse merge(@RequestHeader(value = "X-Anonymous-Cart-Token", required = false) String headerToken,
                       @AuthenticationPrincipal AuthenticatedUser user,
                       HttpServletResponse response) {
        UUID token = headerToken == null || headerToken.isBlank()
                ? UUID.randomUUID()
                : UUID.fromString(headerToken);
        CartResponse merged = anonymousCartService.mergeInto(token, user.id());
        cookieSupport.clearCookie(response);
        return merged;
    }

    private UUID resolveToken(String headerToken, HttpServletResponse response) {
        return UUID.fromString(cookieSupport.resolveToken(headerToken, response));
    }
}
