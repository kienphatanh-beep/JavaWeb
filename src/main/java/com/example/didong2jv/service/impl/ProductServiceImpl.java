package com.example.didong2jv.service.impl;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.didong2jv.entity.*;
import com.example.didong2jv.exceptions.*;
import com.example.didong2jv.payloads.*;
import com.example.didong2jv.repository.*;
import com.example.didong2jv.service.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired private ProductRepo productRepo;
    @Autowired private CategoryRepo categoryRepo;
    @Autowired private CartRepo cartRepo;
    @Autowired private CartService cartService;
    @Autowired private FileService fileService;
    @Autowired private ModelMapper modelMapper;

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${project.image}")
    private String path;

    // --- HÀM HỖ TRỢ MAP DTO (Đóng gói logic chuyển đổi) ---
    private ProductDTO mapToDTO(Product product) {
        ProductDTO dto = modelMapper.map(product, ProductDTO.class);
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getCategoryId());
            dto.setCategoryName(product.getCategory().getCategoryName());
        }
        return dto;
    }

    // =========================================================================
    // 🔥 CÂU 3: THỰC THI TRUY VẤN PHỨC TẠP
    // =========================================================================
    @Override
    public ProductResponse searchProducts(String keyword, Long categoryId, Double minPrice, Double maxPrice, 
                                          Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        
        // Tạo đối tượng Sort động dựa trên tham số truyền vào
        Sort sort = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        // Thực thi câu truy vấn "Thần thánh" từ Repository
        Page<Product> pageProducts = productRepo.searchProducts(keyword, categoryId, minPrice, maxPrice, pageable);

        List<ProductDTO> dtos = pageProducts.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        return new ProductResponse(dtos, pageProducts.getNumber(), pageProducts.getSize(), 
                                   pageProducts.getTotalElements(), pageProducts.getTotalPages(), pageProducts.isLast());
    }

    // =========================================================================
    // 🔥 CÁC HÀM CRUD VÀ LOGIC NGHIỆP VỤ (GIỮ NGUYÊN & TỐI ƯU)
    // =========================================================================

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class) 
    public ProductDTO updateProduct(Long productId, Product product) {
        entityManager.clear(); 
        
        Product dbProduct = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        // Cập nhật thông tin cơ bản
        dbProduct.setProductName(product.getProductName());
        dbProduct.setDescription(product.getDescription());
        dbProduct.setQuantity(product.getQuantity());
        dbProduct.setPrice(product.getPrice());
        dbProduct.setDiscount(product.getDiscount());
        
        // Tự động tính toán SpecialPrice (Logic đóng gói)
        dbProduct.setSpecialPrice(product.getPrice() - ((product.getDiscount() * 0.01) * product.getPrice()));

        if (product.getCategoryId() != null) {
             Category newCategory = categoryRepo.findById(product.getCategoryId())
                      .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", product.getCategoryId()));
             dbProduct.setCategory(newCategory);
        }

        if (product.getImage() != null && !product.getImage().isEmpty()) {
            dbProduct.setImage(product.getImage());
        }

        Product saved = productRepo.saveAndFlush(dbProduct);
        
        // 🔥 ĐỒNG BỘ GIỎ HÀNG: Cập nhật giá mới cho khách hàng
        try {
            List<Cart> carts = cartRepo.findCartsByProductId(productId);
            if (carts != null && !carts.isEmpty()) {
                carts.forEach(cart -> cartService.updateProductInCarts(cart.getCartId(), productId));
            }
        } catch (Exception e) {
            // Không chặn tiến trình nếu lỗi đồng bộ giỏ hàng
        }

        return mapToDTO(saved);
    }

    @Override
    @Transactional
    public ProductDTO addProduct(Long categoryId, Product product) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        
        // Kiểm tra trùng tên (Sử dụng Basic Query Method)
        if (productRepo.existsByProductName(product.getProductName())) {
            throw new APIException("Sản phẩm '" + product.getProductName() + "' đã tồn tại!");
        }
        
        if(product.getImage() == null || product.getImage().isEmpty()) product.setImage("default.png");
        
        product.setCategory(category);
        product.setSpecialPrice(product.getPrice() - ((product.getDiscount() * 0.01) * product.getPrice()));
        
        return mapToDTO(productRepo.save(product));
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        // Tái sử dụng hàm search với các tham số null
        return searchProducts(null, null, null, null, pageNumber, pageSize, sortBy, sortOrder);
    }

    @Override
    public ProductDTO getProductById(Long productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        return mapToDTO(product);
    }

    @Override
    public InputStream getProductImage(String fileName) throws IOException {
        String fullPath = path + File.separator + fileName;
        File file = new File(fullPath);
        if (!file.exists()) throw new FileNotFoundException("File not found: " + fileName);
        return new FileInputStream(file);
    }

    @Override
    @Transactional
    public String deleteProduct(Long productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        
        // Xóa khỏi giỏ hàng trước khi xóa vĩnh viễn
        try {
            List<Cart> carts = cartRepo.findCartsByProductId(productId);
            if (carts != null) {
                carts.forEach(c -> cartService.deleteProductFromCart(c.getCartId(), productId));
            }
        } catch (Exception e) {}
        
        productRepo.delete(product);
        return "Product '" + product.getProductName() + "' deleted successfully!";
    }

    @Override
    @Transactional
    public ProductDTO updateProductImage(Long productId, MultipartFile image) throws IOException {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        
        String fileName = fileService.uploadImage(path, image);
        product.setImage(fileName);
        return mapToDTO(productRepo.save(product));
    }
    
    @Override
    public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        return searchProducts("", categoryId, null, null, pageNumber, pageSize, sortBy, sortOrder);
    }

    @Override
    public ProductResponse searchProductByKeyword(String keyword, Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        return searchProducts(keyword, categoryId, null, null, pageNumber, pageSize, sortBy, sortOrder);
    }
}