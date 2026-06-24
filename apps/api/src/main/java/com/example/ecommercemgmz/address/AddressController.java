package com.example.ecommercemgmz.address;

import com.example.ecommercemgmz.auth.AuthenticatedUser;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
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
@RequestMapping("/api/me/addresses")
public class AddressController {
    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping
    List<AddressResponse> findAddresses(@AuthenticationPrincipal AuthenticatedUser user) {
        return addressService.findCustomerAddresses(user.id());
    }

    @PostMapping
    ResponseEntity<AddressResponse> create(@AuthenticationPrincipal AuthenticatedUser user, @Valid @RequestBody AddressRequest request) {
        AddressResponse response = addressService.create(user.id(), request);
        return ResponseEntity.created(URI.create("/api/me/addresses/" + response.id())).body(response);
    }

    @PutMapping("/{addressId}")
    AddressResponse update(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long addressId, @Valid @RequestBody AddressRequest request) {
        return addressService.update(user.id(), addressId, request);
    }

    @PutMapping("/{addressId}/default")
    AddressResponse setDefault(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long addressId) {
        return addressService.setDefault(user.id(), addressId);
    }

    @DeleteMapping("/{addressId}")
    ResponseEntity<Void> delete(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long addressId) {
        addressService.delete(user.id(), addressId);
        return ResponseEntity.noContent().build();
    }
}
