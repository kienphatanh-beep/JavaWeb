package com.example.didong2jv;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.didong2jv.config.AppConstants;
import com.example.didong2jv.entity.Role;
import com.example.didong2jv.repository.RoleRepo;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

import java.util.List;

@SpringBootApplication
@SecurityScheme(name = "E-Commerce Application", scheme = "bearer", type = SecuritySchemeType.HTTP, in = SecuritySchemeIn.HEADER)
public class Didong2jvApplication implements CommandLineRunner {

    @Autowired
    private RoleRepo roleRepo;

    public static void main(String[] args) {
        SpringApplication.run(Didong2jvApplication.class, args);
    }

    // --- ĐÃ XÓA BEAN ModelMapper TẠI ĐÂY VÌ ĐÃ CÓ TRONG AppConfig.java ---

   @Override
    public void run(String... args) throws Exception {
        try {
            // 1. Xử lý Role ADMIN
            Role adminRole = roleRepo.findById(AppConstants.ADMIN_ID).orElse(null);
            if (adminRole == null) {
                // Chỉ tạo mới nếu chưa tìm thấy ID 101
                adminRole = new Role();
                adminRole.setRoleId(AppConstants.ADMIN_ID);
                adminRole.setRoleName("ADMIN");
                roleRepo.save(adminRole);
                System.out.println(">>> Đã tạo Role ADMIN (ID: " + AppConstants.ADMIN_ID + ")");
            } else {
                System.out.println(">>> Role ADMIN đã tồn tại, bỏ qua.");
            }

            // 2. Xử lý Role USER
            Role userRole = roleRepo.findById(AppConstants.USER_ID).orElse(null);
            if (userRole == null) {
                // Chỉ tạo mới nếu chưa tìm thấy ID 102
                userRole = new Role();
                userRole.setRoleId(AppConstants.USER_ID);
                userRole.setRoleName("USER");
                roleRepo.save(userRole);
                System.out.println(">>> Đã tạo Role USER (ID: " + AppConstants.USER_ID + ")");
            } else {
                System.out.println(">>> Role USER đã tồn tại, bỏ qua.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}