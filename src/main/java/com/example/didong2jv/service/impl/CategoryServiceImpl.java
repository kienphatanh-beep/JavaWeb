package com.example.didong2jv.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; // Import Value
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile; // Import MultipartFile

import com.example.didong2jv.entity.Category;
import com.example.didong2jv.entity.Product;
import com.example.didong2jv.exceptions.ResourceNotFoundException;
import com.example.didong2jv.payloads.CategoryDTO;
import com.example.didong2jv.payloads.CategoryResponse;
import com.example.didong2jv.repository.CategoryRepo;
import com.example.didong2jv.service.CategoryService;
import com.example.didong2jv.service.FileService; // Import FileService
import com.example.didong2jv.service.ProductService;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepo categoryRepo;

    @Autowired
    private ProductService productService;
    
    @Autowired
    private FileService fileService; // Inject FileService đã có

    @Autowired
    private ModelMapper modelMapper;

    // Lấy đường dẫn từ application.properties
    @Value("${project.image}")
    private String path;

    @Override
    public CategoryDTO createCategory(Category category, MultipartFile image) {
        // Xử lý ảnh
        String fileName = "default.png";
        
        if (image != null && !image.isEmpty()) {
            try {
                fileName = fileService.uploadImage(path, image);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Error uploading image");
            }
        }
        
        category.setCategoryImage(fileName);
        Category savedCategory = categoryRepo.save(category);

        return modelMapper.map(savedCategory, CategoryDTO.class);
    }

    @Override
    public CategoryDTO updateCategory(Category category, Long categoryId, MultipartFile image) {

        Category existingCategory = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        // Cập nhật tên
        existingCategory.setCategoryName(category.getCategoryName());

        // Cập nhật ảnh nếu có file mới gửi lên
        if (image != null && !image.isEmpty()) {
            try {
                String fileName = fileService.uploadImage(path, image);
                existingCategory.setCategoryImage(fileName);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Error uploading image");
            }
        }
        // Nếu image null thì giữ nguyên ảnh cũ

        Category savedCategory = categoryRepo.save(existingCategory);
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }

    // ... Các hàm getCategories, getCategoryById, deleteCategory GIỮ NGUYÊN KHÔNG ĐỔI ...
    @Override
    public CategoryResponse getCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Category> pageCategories = categoryRepo.findAll(pageDetails);
        List<Category> categories = pageCategories.getContent();
        List<CategoryDTO> categoryDTOs = categories.stream().map(cat -> modelMapper.map(cat, CategoryDTO.class)).collect(Collectors.toList());
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTOs);
        categoryResponse.setPageNumber(pageCategories.getNumber());
        categoryResponse.setPageSize(pageCategories.getSize());
        categoryResponse.setTotalElements(pageCategories.getTotalElements());
        categoryResponse.setTotalPages(pageCategories.getTotalPages());
        categoryResponse.setLastPage(pageCategories.isLast());
        return categoryResponse;
    }

    @Override
    public CategoryDTO getCategoryById(Long categoryId) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        return modelMapper.map(category, CategoryDTO.class);
    }

    @Override
    public String deleteCategory(Long categoryId) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        // Xóa các sản phẩm con (logic cũ của bạn)
        List<Product> products = category.getProducts();
        products.forEach(product -> productService.deleteProduct(product.getProductId()));
        
        categoryRepo.delete(category);
        return "Category with categoryId: " + categoryId + " deleted successfully !!!";
    }
}