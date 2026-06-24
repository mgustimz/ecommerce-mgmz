package com.example.ecommercemgmz.cart;

import com.example.ecommercemgmz.common.ApiException;
import com.example.ecommercemgmz.product.Product;
import com.example.ecommercemgmz.product.ProductService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    public CartService(CartItemRepository cartItemRepository, ProductService productService) {
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
    }

    @Transactional(readOnly = true)
    public CartResponse findCart(Long customerId) {
        return CartResponse.from(customerId, cartItemRepository.findByCustomerIdOrderByIdAsc(customerId));
    }

    @Transactional
    public CartResponse addItem(Long customerId, AddCartItemRequest request) {
        Product product = productService.findEntity(request.productId());
        validateAvailable(product, request.quantity());

        CartItem item = cartItemRepository.findByCustomerIdAndProductId(customerId, request.productId())
                .orElseGet(() -> new CartItem(customerId, product, 0));
        int newQuantity = item.getQuantity() + request.quantity();
        validateAvailable(product, newQuantity);
        item.setQuantity(newQuantity);
        cartItemRepository.save(item);
        return buildCartResponse(customerId);
    }

    @Transactional
    public CartResponse updateItem(Long customerId, Long itemId, UpdateCartItemRequest request) {
        CartItem item = findItem(itemId, customerId);
        validateAvailable(item.getProduct(), request.quantity());
        item.setQuantity(request.quantity());
        return buildCartResponse(customerId);
    }

    @Transactional
    public void removeItem(Long customerId, Long itemId) {
        CartItem item = findItem(itemId, customerId);
        cartItemRepository.delete(item);
    }

    public List<CartItem> findItemsForCheckout(Long customerId) {
        return cartItemRepository.findByCustomerIdOrderByIdAsc(customerId);
    }

    public void clear(Long customerId) {
        cartItemRepository.deleteByCustomerId(customerId);
    }

    private CartItem findItem(Long itemId, Long customerId) {
        return cartItemRepository.findByIdAndCustomerId(itemId, customerId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cart item not found"));
    }

    private CartResponse buildCartResponse(Long customerId) {
        return CartResponse.from(customerId, cartItemRepository.findByCustomerIdOrderByIdAsc(customerId));
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
