package com.example.didong2jv.payloads;

import java.util.HashSet;
import java.util.Set;
import com.example.didong2jv.entity.Role;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long userId;
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String email;
    private String password;
    
    // 🔥 MỚI: Thêm trường image vào DTO để trả về cho Frontend
    private String image;
    
    private Set<Role> roles = new HashSet<>();
    private AddressDTO address;
}