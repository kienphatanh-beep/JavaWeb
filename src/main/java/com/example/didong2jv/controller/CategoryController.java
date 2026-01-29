package com.example.didong2jv.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType; 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.didong2jv.config.AppConstants;
import com.example.didong2jv.entity.Category;
import com.example.didong2jv.payloads.CategoryDTO;
import com.example.didong2jv.payloads.CategoryResponse;
import com.example.didong2jv.service.CategoryService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "E-Commerce Application")
@CrossOrigin(origins = "*", exposedHeaders = "X-Total-Count")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // ... (Giữ nguyên các API Create, Update, Delete của bạn) ...
    @PostMapping(value = "/admin/categories", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CategoryDTO> createCategory(
            @RequestParam("categoryName") String categoryName,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        Category category = new Category();
        category.setCategoryName(categoryName);
        CategoryDTO savedCategoryDTO = categoryService.createCategory(category, image);
        return new ResponseEntity<>(savedCategoryDTO, HttpStatus.CREATED);
    }

    @PutMapping(value = "/admin/categories/{categoryId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CategoryDTO> updateCategory(
            @PathVariable Long categoryId,
            @RequestParam("categoryName") String categoryName,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        Category category = new Category();
        category.setCategoryName(categoryName);
        CategoryDTO categoryDTO = categoryService.updateCategory(category, categoryId, image);
        return new ResponseEntity<>(categoryDTO, HttpStatus.OK);
    }
    
    @DeleteMapping("/admin/categories/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long categoryId) {
        String status = categoryService.deleteCategory(categoryId);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }

    // ========================================================================
    // API LẤY ẢNH (Đã thêm từ bước trước)
    // ========================================================================
    @GetMapping("/public/categories/image/{fileName}")
    public ResponseEntity<InputStreamResource> getCategoryImage(@PathVariable String fileName) throws IOException {
        Path filePath = Paths.get("images").resolve(fileName);
        if (!Files.exists(filePath)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        InputStreamResource resource = new InputStreamResource(Files.newInputStream(filePath));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG); 
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    // ========================================================================
    // 🔥 SỬA LỖI TẠI ĐÂY (GET LIST)
    // ========================================================================
    @GetMapping("/public/categories")
    public ResponseEntity<CategoryResponse> getCategories(
            @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_CATEGORIES_BY, required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {

        CategoryResponse categoryResponse = categoryService.getCategories(
                pageNumber == 0 ? pageNumber : pageNumber - 1,
                pageSize,
                "id".equals(sortBy) ? "categoryId" : sortBy,
                sortOrder
        );
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count", String.valueOf(categoryResponse.getTotalElements()));

        // ✅ FIX: Trả về object `categoryResponse` thay vì `categoryResponse.getContent()`
        // Điều này khớp với khai báo `ResponseEntity<CategoryResponse>` của hàm
        return new ResponseEntity<>(categoryResponse, headers, HttpStatus.OK);
    }

    @GetMapping("/public/categories/{categoryId}")
    public ResponseEntity<CategoryDTO> getOneCategory(@PathVariable Long categoryId) {
        CategoryDTO categoryDTO = categoryService.getCategoryById(categoryId);
        return new ResponseEntity<>(categoryDTO, HttpStatus.OK);
    }
}