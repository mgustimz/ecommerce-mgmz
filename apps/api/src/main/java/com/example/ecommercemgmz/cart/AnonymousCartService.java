package com.example.ecommercemgmz.cart;

import com.example.ecommercemgmz.common.ApiException;
import com.example.ecommercemgmz.product.Product;
import com.example.ecommercemgmz.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AnonymousCartService {
    private final AnonymousCartRepository anonymousCartRepository;
    private final AnonymousCartItemRepository anonymousCartItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    @Transactional(readOnly = true)
    public CartResponse get(UUID token) {
        return buildResponse(findOrCreate(token));
    }

    @Transactional
    public CartResponse addItem(UUID token, AddCartItemRequest request) {
        Product product = productService.findEntity(request.productId());
        validateAvailable(product, request.quantity());
        AnonymousCart cart = findOrCreate(token);
        cart.addOrIncrement(product, request.quantity());
        validateAvailable(product, quantityOf(cart, product.getId()));
        anonymousCartRepository.save(cart);
        return buildResponse(cart);
    }

    @Transactional
    public CartResponse updateItem(UUID token, Long itemId, UpdateCartItemRequest request) {
        AnonymousCart cart = findOrCreate(token);
        AnonymousCartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cart item not found"));
        validateAvailable(item.getProduct(), request.quantity());
        item.setQuantity(request.quantity());
        anonymousCartRepository.save(cart);
        return buildResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(UUID token, Long itemId) {
        AnonymousCart cart = findOrCreate(token);
        cart.getItems().removeIf(i -> i.getId().equals(itemId));
        anonymousCartRepository.save(cart);
        return buildResponse(cart);
    }

    @Transactional
    public CartResponse mergeInto(UUID token, Long customerId) {
        AnonymousCart cart = anonymousCartRepository.findById(token).orElse(null);
        if (cart == null || cart.getItems().isEmpty()) {
            return CartResponse.from(customerId, cartItemRepository.findByCustomerIdOrderByIdAsc(customerId));
        }

        for (AnonymousCartItem anonymousItem : cart.getItems()) {
            Product product = anonymousItem.getProduct();
            CartItem customerItem = cartItemRepository.findByCustomerIdAndProductId(customerId, product.getId())
                    .orElseGet(() -> new CartItem(customerId, product, 0));
            int newQuantity = customerItem.getQuantity() + anonymousItem.getQuantity();
            validateAvailable(product, newQuantity);
            customerItem.setQuantity(newQuantity);
            cartItemRepository.save(customerItem);
        }

        anonymousCartRepository.delete(cart);
        return CartResponse.from(customerId, cartItemRepository.findByCustomerIdOrderByIdAsc(customerId));
    }

    private AnonymousCart findOrCreate(UUID token) {
        return anonymousCartRepository.findById(token).orElseGet(() -> anonymousCartRepository.save(new AnonymousCart(token)));
    }

    private int quantityOf(AnonymousCart cart, Long productId) {
        return cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .mapToInt(AnonymousCartItem::getQuantity)
                .sum();
    }

    private CartResponse buildResponse(AnonymousCart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> new CartItemResponse(
                        item.getId(),
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getProduct().getPrice(),
                        item.getQuantity(),
                        item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();
        BigDecimal subtotal = items.stream()
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new CartResponse(null, items, subtotal);
    }

    private void validateAvailable(Product product, int quantity) {
        if (!product.isPublished()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Product is not active");
        }
        if (product.getStock() < quantity) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Insufficient stock for " + product.getName());
        }
    }
}
