package com.example.didong2jv.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.example.didong2jv.entity.Cart;

@Repository
public interface CartRepo extends JpaRepository<Cart, Long> {
    
    // Tìm giỏ hàng cụ thể theo Email và CartId (cũ)
    @Query("SELECT c FROM Cart c WHERE c.user.email = ?1 AND c.cartId = ?2")
    Cart findCartByEmailAndCartId(String email, Long cartId);

    // 🔥 MỚI: Tìm giỏ hàng chỉ bằng Email (Để lấy CartId)
    @Query("SELECT c FROM Cart c WHERE c.user.email = ?1")
    Cart findCartByEmail(String email);

    // Tìm các giỏ hàng có chứa sản phẩm cụ thể
    @Query("SELECT c FROM Cart c JOIN FETCH c.cartItems ci JOIN FETCH ci.product p WHERE p.productId = ?1")
    List<Cart> findCartsByProductId(Long productId);
}