package com.example.didong2jv.service;

import java.util.List;
import com.example.didong2jv.payloads.CartDTO;

public interface CartService {
    // --- Các hàm cho USER (Dùng Token) ---
    CartDTO getCartByLoggedInUser();
    CartDTO addProductToCartForUser(Long productId, Integer quantity);
    CartDTO updateProductQuantityInCartForUser(Long productId, Integer quantity);
    String deleteProductFromCartForUser(Long productId);

    // --- Các hàm cho ADMIN & HỆ THỐNG ---
    List<CartDTO> getAllCarts();
    
    // 🔥 QUAN TRỌNG: Hàm này dùng để cập nhật giá sản phẩm trong tất cả giỏ hàng khi Admin sửa giá
    void updateProductInCarts(Long cartId, Long productId);

    // --- Các hàm bổ trợ logic ---
    CartDTO addProductToCart(Long cartId, Long productId, Integer quantity);
    CartDTO updateProductQuantityInCart(Long cartId, Long productId, Integer quantity);
    String deleteProductFromCart(Long cartId, Long productId);
    CartDTO getCart(String emailId, Long cartId);
}