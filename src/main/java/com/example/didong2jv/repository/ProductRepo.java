package com.example.didong2jv.repository;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.example.didong2jv.entity.Product;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {

    // =========================================================================
    // 1. CÁC CÂU TRUY VẤN CƠ BẢN (Derived Query Methods) CRUD CƠ BẢN
    // =========================================================================
    
    // Tìm sản phẩm theo tên chính xác
    Product findByProductName(String productName);

    // Kiểm tra sự tồn tại của sản phẩm theo tên (Dùng cho logic validation)
    boolean existsByProductName(String productName);

    // Tìm tất cả sản phẩm thuộc một Category ID cụ thể
    List<Product> findByCategoryCategoryId(Long categoryId);

    // =========================================================================
    // 2. CÂU TRUY VẤN PHỨC TẠP (JPQL - Search + Filter + Sort + Pagination)
    // =========================================================================
    @Query("SELECT p FROM Product p WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR lower(p.productName) LIKE lower(concat('%', :keyword, '%'))) " +
           "AND (:categoryId IS NULL OR p.category.categoryId = :categoryId) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable // Tự động xử lý ORDER BY và LIMIT/OFFSET
    );
}