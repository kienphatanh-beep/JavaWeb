package com.example.didong2jv.payloads;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
    private Long categoryId;
    private String categoryName;
    
    // --- THÊM ĐOẠN NÀY (Tên phải trùng với Entity để ModelMapper tự map) ---
    private String categoryImage;
    // -----------------------------------------------------------------------
}