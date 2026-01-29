package com.example.didong2jv.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.didong2jv.entity.Order;

@Repository
public interface OrderRepo extends JpaRepository<Order, Long> {

    // Đã cập nhật o.id thành o.orderId để khớp với tên thuộc tính trong Entity
    @Query("SELECT o FROM Order o WHERE o.email = ?1 AND o.orderId = ?2")
    Order findOrderByEmailAndOrderId(String email, Long orderId);

    List<Order> findAllByEmail(String emailId);
}