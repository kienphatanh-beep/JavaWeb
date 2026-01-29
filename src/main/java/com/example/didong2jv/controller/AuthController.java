package com.example.didong2jv.controller;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; 
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; 

import com.example.didong2jv.payloads.AddressDTO; 
import com.example.didong2jv.payloads.UserDTO;
import com.example.didong2jv.security.JWTUtil;
import com.example.didong2jv.service.UserService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "E-Commerce Application")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String path = "src/main/resources/static/images/"; 

    // --- API REGISTER ---
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> registerHandler(
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("firstName") String firstName, 
            @RequestParam("lastName") String lastName,
            @RequestParam("phone") String mobileNumber,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "roleId", defaultValue = "102", required = false) Long roleId
    ) {
        try {
            UserDTO user = new UserDTO();
            user.setEmail(email);
            user.setPassword(password); 
            user.setFirstName(firstName); 
            user.setLastName(lastName); 
            user.setMobileNumber(mobileNumber);

            // TẠO ĐỊA CHỈ MẶC ĐỊNH
            AddressDTO address = new AddressDTO();
            address.setCountry("Vietnam");
            address.setCity("Ho Chi Minh");
            address.setStreet("Unknown Street"); // Sửa cho dài hơn xíu
            address.setState("Default State");
            address.setPincode("700000"); 
            
            // [QUAN TRỌNG] Sửa "None" thành chuỗi dài hơn 5 ký tự để qua validation
            address.setBuildingName("Default Building"); 

            user.setAddress(address);

            // --- XỬ LÝ LƯU ẢNH ---
            if (image != null && !image.isEmpty()) {
                String originalFileName = StringUtils.cleanPath(image.getOriginalFilename());
                String uniqueFileName = System.currentTimeMillis() + "_" + originalFileName;
                user.setImage(uniqueFileName); 

                try {
                    Path uploadPath = Paths.get(path);
                    if (!Files.exists(uploadPath)) {
                        Files.createDirectories(uploadPath);
                    }
                    try (InputStream inputStream = image.getInputStream()) {
                        Path filePath = uploadPath.resolve(uniqueFileName);
                        Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (Exception e) {
                    System.err.println("❌ Lỗi lưu file: " + e.getMessage());
                }
            } else {
                user.setImage("default.png");
            }

            userService.registerUser(user, roleId);
            
            return new ResponseEntity<>(Collections.singletonMap("message", "Đăng ký thành công"), HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace(); 
            return new ResponseEntity<>(Collections.singletonMap("message", "Đăng ký thất bại: " + e.getMessage()), HttpStatus.BAD_REQUEST);
        }
    }

    // --- API LOGIN ---
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> loginHandler(
            @RequestParam("email") String email, 
            @RequestParam("password") String password
    ) {
        try {
            UsernamePasswordAuthenticationToken authCredentials = new UsernamePasswordAuthenticationToken(email, password);
            var authentication = authenticationManager.authenticate(authCredentials);
            String token = jwtUtil.generateToken(email);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(item -> item.getAuthority())
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("jwt-token", token);
            response.put("message", "Đăng nhập thành công!"); 
            response.put("email", email);
            response.put("roles", roles);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(
                Collections.singletonMap("message", "Sai tài khoản hoặc mật khẩu!"), 
                HttpStatus.UNAUTHORIZED
            );
        }
    }
}