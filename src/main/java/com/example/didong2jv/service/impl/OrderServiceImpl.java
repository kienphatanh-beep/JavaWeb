package com.example.didong2jv.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.didong2jv.entity.*;
import com.example.didong2jv.exceptions.APIException;
import com.example.didong2jv.exceptions.ResourceNotFoundException;
import com.example.didong2jv.payloads.OrderDTO;
import com.example.didong2jv.payloads.OrderItemDTO;
import com.example.didong2jv.payloads.OrderResponse;
import com.example.didong2jv.repository.*;
import com.example.didong2jv.service.CartService;
import com.example.didong2jv.service.OrderService;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired private CartRepo cartRepo;
    @Autowired private OrderRepo orderRepo;
    @Autowired private PaymentRepo paymentRepo;
    @Autowired private CartItemRepo cartItemRepo;
    @Autowired private ProductRepo productRepo;
    @Autowired private CartService cartService;
    @Autowired private ModelMapper modelMapper;

    /**
     * 🔥 Logic Đặt Hàng Bảo Mật
     * 1. Lấy danh tính người dùng từ Token.
     * 2. Lọc sản phẩm được chọn từ giỏ hàng.
     * 3. Tính toán tổng tiền: $Total = \sum (Price_{item} \times Quantity_{item})$
     * 4. Trừ kho và xóa item đã mua khỏi giỏ hàng.
     */
    @Override
    @Transactional
    public OrderDTO placeOrder(String paymentMethod, List<Long> productIds) {
        // 1. Lấy email từ Security Context (Bảo mật: Không dùng email từ URL)
        String emailId = SecurityContextHolder.getContext().getAuthentication().getName();

        // 2. Tìm giỏ hàng dựa trên email
        Cart cart = cartRepo.findCartByEmail(emailId);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "email", emailId);
        }

        // 3. Lọc danh sách CartItem mà User đã chọn mua (Checkbox ở Frontend)
        List<CartItem> selectedCartItems = cart.getCartItems().stream()
                .filter(item -> productIds.contains(item.getProduct().getProductId()))
                .collect(Collectors.toList());

        if (selectedCartItems.isEmpty()) {
            throw new APIException("Vui lòng chọn ít nhất một sản phẩm để thanh toán!");
        }

        // 4. Tính tổng số tiền các món đã chọn
        double totalAmountSelected = selectedCartItems.stream()
                .mapToDouble(item -> item.getProductPrice() * item.getQuantity())
                .sum();

        // 5. Khởi tạo Đơn hàng (Order)
        Order order = new Order();
        order.setEmail(emailId);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(totalAmountSelected);
        order.setOrderStatus("Order Accepted!");

        // 6. Tạo thông tin thanh toán (Payment)
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(paymentMethod);
        payment = paymentRepo.save(payment);
        order.setPayment(payment);

        Order savedOrder = orderRepo.save(order);
        List<OrderItem> orderItems = new ArrayList<>();

        // 7. Chuyển đổi CartItem -> OrderItem & Xử lý kho
        for (CartItem cartItem : selectedCartItems) {
            Product product = cartItem.getProduct();

            // Kiểm tra tồn kho
            if (product.getQuantity() < cartItem.getQuantity()) {
                throw new APIException("Sản phẩm " + product.getProductName() + " không đủ hàng (Còn lại: " + product.getQuantity() + ")");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
            orderItem.setOrder(savedOrder);
            
            orderItems.add(orderItem);

            // Trừ số lượng trong kho
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productRepo.save(product);

            // 🔥 Xóa sản phẩm ĐÃ MUA ra khỏi giỏ hàng thực tế
            // Lưu ý: Sản phẩm không được chọn vẫn ở lại trong giỏ.
            cartService.deleteProductFromCart(cart.getCartId(), product.getProductId());
        }

        savedOrder.setOrderItems(orderItems);
        orderRepo.save(savedOrder);

        // 8. Trả về kết quả
        OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
        orderDTO.setOrderItems(orderItems.stream()
                .map(item -> modelMapper.map(item, OrderItemDTO.class))
                .collect(Collectors.toList()));

        return orderDTO;
    }

    @Override
    public List<OrderDTO> getOrdersByUser(String emailId) {
        List<Order> orders = orderRepo.findAllByEmail(emailId);
        if (orders.isEmpty()) throw new APIException("Không tìm thấy đơn hàng nào cho user: " + emailId);
        return orders.stream().map(o -> modelMapper.map(o, OrderDTO.class)).collect(Collectors.toList());
    }

    @Override
    public OrderDTO getOrder(String emailId, Long orderId) {
        Order order = orderRepo.findOrderByEmailAndOrderId(emailId, orderId);
        if (order == null) throw new ResourceNotFoundException("Order", "id", orderId);
        return modelMapper.map(order, OrderDTO.class);
    }

    @Override
    public OrderResponse getAllOrders(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Order> pageOrders = orderRepo.findAll(pageable);

        List<OrderDTO> dtos = pageOrders.getContent().stream()
                .map(o -> modelMapper.map(o, OrderDTO.class))
                .collect(Collectors.toList());

        OrderResponse response = new OrderResponse();
        response.setContent(dtos);
        response.setPageNumber(pageOrders.getNumber());
        response.setPageSize(pageOrders.getSize());
        response.setTotalElements(pageOrders.getTotalElements());
        response.setTotalPages(pageOrders.getTotalPages());
        response.setLastPage(pageOrders.isLast());
        return response;
    }

    @Override
    public OrderDTO updateOrder(String emailId, Long orderId, String orderStatus) {
        Order order = orderRepo.findOrderByEmailAndOrderId(emailId, orderId);
        if (order == null) throw new ResourceNotFoundException("Order", "id", orderId);
        order.setOrderStatus(orderStatus);
        return modelMapper.map(orderRepo.save(order), OrderDTO.class);
    }
}