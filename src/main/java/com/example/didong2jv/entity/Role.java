package com.example.didong2jv.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    // --- XÓA HOẶC COMMENT DÒNG DƯỚI ĐÂY ---
    // @GeneratedValue(strategy = GenerationType.IDENTITY) 
    // ---------------------------------------
    private Long roleId;
    
    private String roleName;
}