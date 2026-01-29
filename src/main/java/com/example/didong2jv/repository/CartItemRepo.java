package com.example.didong2jv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.didong2jv.entity.CartItem;
import com.example.didong2jv.entity.Product;

import jakarta.transaction.Transactional; // 🔥 Quan trọng: Import Transactional

@Repository
public interface CartItemRepo extends JpaRepository<CartItem, Long> {

    @Query("SELECT ci.product FROM CartItem ci WHERE ci.product.productId = ?1")
    Product findProductById(Long productId);

    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.cartId = ?1 AND ci.product.productId = ?2")
    CartItem findCartItemByProductIdAndCartId(Long cartId, Long productId);

    // 🔥 SỬA LỖI XÓA Ở ĐÂY
    @Modifying // Bắt buộc cho lệnh DELETE/UPDATE
    @Transactional // Bắt buộc để commit transaction
    @Query("DELETE FROM CartItem ci WHERE ci.cart.cartId = ?2 AND ci.product.productId = ?1")
    void deleteCartItemByProductIdAndCartId(Long productId, Long cartId);
}