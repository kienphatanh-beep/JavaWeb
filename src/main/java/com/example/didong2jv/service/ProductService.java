package com.example.didong2jv.service;

import com.example.didong2jv.entity.Product;
import com.example.didong2jv.payloads.ProductDTO;
import com.example.didong2jv.payloads.ProductResponse;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;

public interface ProductService {
    ProductDTO addProduct(Long categoryId, Product product);
    ProductDTO updateProduct(Long productId, Product product);
    String deleteProduct(Long productId);
    ProductDTO getProductById(Long productId);
    ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    
    // --- CÁC HÀM CŨ (CÓ THỂ GIỮ LẠI ĐỂ TƯƠNG THÍCH MOBILE APP CŨ) ---
    ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    ProductResponse searchProductByKeyword(String keyword, Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    
    // 🔥 HÀM MỚI QUAN TRỌNG NHẤT: TÌM KIẾM TỔNG HỢP CHO WEBSITE
    ProductResponse searchProducts(String keyword, Long categoryId, Double minPrice, Double maxPrice, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException;
    InputStream getProductImage(String fileName) throws IOException;
}