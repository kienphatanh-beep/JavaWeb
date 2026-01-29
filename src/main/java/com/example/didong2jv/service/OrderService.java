package com.example.didong2jv.service;

import java.util.List;
import com.example.didong2jv.payloads.OrderDTO;
import com.example.didong2jv.payloads.OrderResponse;

public interface OrderService {
    // 🔥 Chỉ nhận Phương thức thanh toán và Danh sách ID sản phẩm được chọn
    OrderDTO placeOrder(String paymentMethod, List<Long> productIds);
    
    OrderDTO getOrder(String emailId, Long orderId);
    
    List<OrderDTO> getOrdersByUser(String emailId);
    
    OrderResponse getAllOrders(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    
    OrderDTO updateOrder(String emailId, Long orderId, String orderStatus);
}