package com.example.didong2jv.repository; // Cập nhật theo đường dẫn dự án của bạn

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.didong2jv.entity.Role;

@Repository
public interface RoleRepo extends JpaRepository<Role, Long> {
    // JpaRepository sẽ tự động cung cấp các hàm save, findById, delete...
}