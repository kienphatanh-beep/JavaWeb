package com.example.didong2jv.controller;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.didong2jv.config.AppConstants;
import com.example.didong2jv.payloads.UserDTO;
import com.example.didong2jv.payloads.UserResponse;
import com.example.didong2jv.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "E-Commerce Application")
public class UserController {

    @Autowired
    private UserService userService;

    private final String path = "images/"; 

    // --- LẤY DANH SÁCH USER (ADMIN) ---
    @GetMapping("/admin/users")
    public ResponseEntity<UserResponse> getUsers(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_USERS_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {
        
        UserResponse userResponse = userService.getAllUsers(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(userResponse, HttpStatus.OK);
    }

    // --- LẤY USER THEO ID ---
    @GetMapping("/public/users/{userId}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long userId) {
        UserDTO user = userService.getUserById(userId);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    // ========================================================================
    // 🔥 [MỚI] LẤY USER THEO EMAIL (QUAN TRỌNG CHO LOGIN)
    // ========================================================================
    @GetMapping("/public/users/email/{email}")
    public ResponseEntity<UserDTO> getUserByEmail(@PathVariable String email) {
        // Bạn cần đảm bảo UserService đã có hàm này
        UserDTO user = userService.getUserByEmail(email);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    // --- UPLOAD ẢNH ---
    @PostMapping(value = "/public/users/{userId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserDTO> uploadUserImage(
            @PathVariable Long userId,
            @RequestParam("image") MultipartFile image) throws IOException {
        
        String originalFileName = StringUtils.cleanPath(image.getOriginalFilename());
        String uniqueFileName = "user_" + userId + "_" + System.currentTimeMillis() + "_" + originalFileName;

        Path uploadPath = Paths.get(path);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        try (InputStream inputStream = image.getInputStream()) {
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setImage(uniqueFileName);
        UserDTO updatedUser = userService.updateUser(userId, userDTO);
        
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    // --- XEM ẢNH ---
    @GetMapping(value = "/public/users/image/{imageName}", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<InputStreamResource> getUserImage(@PathVariable String imageName) throws IOException {
        Path imagePath = Paths.get(path + imageName);
        if (!Files.exists(imagePath)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        InputStreamResource resource = new InputStreamResource(Files.newInputStream(imagePath));
        return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(resource);
    }

    // --- CẬP NHẬT USER ---
    @PutMapping("/public/users/{userId}")
    public ResponseEntity<UserDTO> updateUser(@RequestBody UserDTO userDTO, @PathVariable Long userId) {
        UserDTO updatedUser = userService.updateUser(userId, userDTO);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    // --- XÓA USER ---
    @DeleteMapping("/admin/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId) {
        String status = userService.deleteUser(userId);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }
}