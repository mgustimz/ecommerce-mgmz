package com.example.ecommercemgmz.shipping;

import com.example.ecommercemgmz.address.AddressService;
import com.example.ecommercemgmz.address.CustomerAddress;
import com.example.ecommercemgmz.auth.AuthenticatedUser;
import com.example.ecommercemgmz.cart.CartItem;
import com.example.ecommercemgmz.cart.CartService;
import com.example.ecommercemgmz.common.ApiException;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/shipping")
public class ShippingController {
    private final ShippingService shippingService;
    private final AddressService addressService;
    private final CartService cartService;

    public ShippingController(ShippingService shippingService, AddressService addressService, CartService cartService) {
        this.shippingService = shippingService;
        this.addressService = addressService;
        this.cartService = cartService;
    }

    @GetMapping("/areas")
    BiteshipAreasResponse searchAreas(@RequestParam(required = false) String query,
                                      @RequestParam(required = false) String postalCode,
                                      @RequestParam(defaultValue = "single") String type) {
        return shippingService.searchAreas(query, postalCode, type);
    }

    @GetMapping("/couriers")
    BiteshipCouriersResponse getCouriers() {
        return shippingService.getCouriers();
    }

    @PostMapping("/rates")
    List<ShippingRateResponse> getRates(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody ShippingRateRequest request) {
        List<CartItem> cartItems = cartService.findItemsForCheckout(user.id());
        if (cartItems.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cart is empty");
        }
        CustomerAddress address = addressService.findEntity(user.id(), request.addressId());
        BigDecimal subtotal = cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return shippingService.getRates(address, cartItems, subtotal, request);
    }
}
