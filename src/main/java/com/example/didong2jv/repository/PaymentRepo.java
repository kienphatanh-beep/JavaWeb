package com.example.didong2jv.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.didong2jv.entity.Payment;

@Repository
public interface PaymentRepo extends JpaRepository<Payment, Long> {
}