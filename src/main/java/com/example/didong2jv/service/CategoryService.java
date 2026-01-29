package com.example.didong2jv.service;

import org.springframework.web.multipart.MultipartFile; // Import này
import com.example.didong2jv.entity.Category;
import com.example.didong2jv.payloads.CategoryDTO;
import com.example.didong2jv.payloads.CategoryResponse;

public interface CategoryService {

    // CẬP NHẬT: Thêm tham số MultipartFile
    CategoryDTO createCategory(Category category, MultipartFile image);

    CategoryResponse getCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    CategoryDTO getCategoryById(Long categoryId);

    // CẬP NHẬT: Thêm tham số MultipartFile
    CategoryDTO updateCategory(Category category, Long categoryId, MultipartFile image);

    String deleteCategory(Long categoryId);
}