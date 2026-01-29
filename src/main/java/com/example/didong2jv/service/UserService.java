package com.example.didong2jv.service;

import com.example.didong2jv.payloads.UserDTO;
import com.example.didong2jv.payloads.UserResponse;

public interface UserService {
    // 🔥 ĐÃ CẬP NHẬT: Thêm tham số roleId vào hàm đăng ký
    UserDTO registerUser(UserDTO userDTO, Long roleId);

    UserResponse getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    
    UserDTO getUserById(Long userId);
    
    // Hàm lấy user theo email (dùng cho AuthController)
    UserDTO getUserByEmail(String email);
    
    UserDTO updateUser(Long userId, UserDTO userDTO);
    
    String deleteUser(Long userId);
}