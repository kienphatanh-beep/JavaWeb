package com.example.didong2jv.entity;

import java.util.List;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @NotBlank
    @Size(min = 5, message = "Category name must contain at least 5 characters")
    private String categoryName;
    
    // --- THÊM ĐOẠN NÀY ---
    @Column(name = "category_image")
    private String categoryImage; 
    // ---------------------

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Product> products;
}