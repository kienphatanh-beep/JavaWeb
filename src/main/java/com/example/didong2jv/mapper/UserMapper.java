package com.example.didong2jv.mapper;

import com.example.didong2jv.entity.User;
import com.example.didong2jv.payloads.UserDTO;

public class UserMapper {

    public static UserDTO toDTO(User user) {
        return new UserDTO(
                user.getUserId(),
                user.getFirstName(),
                user.getLastName(),
                user.getMobileNumber(),
                user.getEmail(),
                null,
                user.getImage(),
                user.getRoles(),
                null 
        );
    }

    public static User toEntity(UserDTO dto) {
        User user = new User();
        user.setUserId(dto.getUserId());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setMobileNumber(dto.getMobileNumber());
        user.setEmail(dto.getEmail());
        user.setImage(dto.getImage());
        user.setRoles(dto.getRoles());
        return user;
    }
}
