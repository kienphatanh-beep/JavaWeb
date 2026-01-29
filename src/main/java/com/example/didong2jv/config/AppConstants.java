package com.example.didong2jv.config;

public class AppConstants {

    public static final String PAGE_NUMBER = "0";
    public static final String PAGE_SIZE = "100";
    public static final String SORT_CATEGORIES_BY = "categoryId";
    public static final String SORT_PRODUCTS_BY = "productId";
    public static final String SORT_USERS_BY = "userId";
    public static final String SORT_ORDERS_BY = "totalAmount";
    
    public static final String SORT_DIR = "asc";
    public static final Long ADMIN_ID = 101L;
    public static final Long USER_ID = 102L;
    
    // ==========================================
    // CẬP NHẬT QUAN TRỌNG:
    // Thời gian hiệu lực: 24 giờ (tính bằng mili-giây)
    // 24 giờ * 60 phút * 60 giây * 1000 mili-giây
    // ==========================================
    public static final long JWT_TOKEN_VALIDITY = 24 * 60 * 60 * 1000; 
    
    // Các đường dẫn API công khai không cần đăng nhập
    public static final String[] PUBLIC_URLS = { 
        "/v3/api-docs",         
        "/v3/api-docs/**", 
        "/swagger-ui/**", 
        "/swagger-ui.html",     
        "/api/register/**", 
        "/api/login/**" 
    };
    
    public static final String[] USER_URLS = { "/api/public/**" };
    public static final String[] ADMIN_URLS = { "/api/admin/**" };
    
}