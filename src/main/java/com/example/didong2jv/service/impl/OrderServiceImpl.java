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

    @Override
    @Transactional
    public OrderDTO placeOrder(String paymentMethod, List<Long> productIds) {
        String emailId = SecurityContextHolder.getContext().getAuthentication().getName();

        Cart cart = cartRepo.findCartByEmail(emailId);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "email", emailId);
        }

        List<CartItem> selectedCartItems = cart.getCartItems().stream()
                .filter(item -> productIds.contains(item.getProduct().getProductId()))
                .collect(Collectors.toList());

        if (selectedCartItems.isEmpty()) {
            throw new APIException("Vui lòng chọn ít nhất một sản phẩm để thanh toán!");
        }

        // 🔥 TÍNH TIỀN: Luôn lấy giá SpecialPrice (khuyến mãi) để khớp với Frontend
        double totalAmountSelected = selectedCartItems.stream()
                .mapToDouble(item -> item.getProduct().getSpecialPrice() * item.getQuantity())
                .sum();

        Order order = new Order();
        order.setEmail(emailId);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(totalAmountSelected);
        
        // 🔥 LOGIC TRẠNG THÁI: Nếu là VNPay thì để "PENDING PAYMENT"
        if (paymentMethod.equalsIgnoreCase("VNPay")) {
            order.setOrderStatus("PENDING PAYMENT");
        } else {
            order.setOrderStatus("Order Accepted!");
        }

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setPaymentMethod(paymentMethod);
        payment = paymentRepo.save(payment);
        order.setPayment(payment);

        Order savedOrder = orderRepo.save(order);
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartItem cartItem : selectedCartItems) {
            Product product = cartItem.getProduct();

            if (product.getQuantity() < cartItem.getQuantity()) {
                throw new APIException("Sản phẩm " + product.getProductName() + " không đủ hàng!");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderedProductPrice(product.getSpecialPrice());
            orderItem.setOrder(savedOrder);
            orderItems.add(orderItem);

            // 🔥 CHỈ TRỪ KHO VÀ XÓA GIỎ NẾU LÀ TIỀN MẶT (CASH)
            // Nếu là VNPay, việc này sẽ được xử lý trong hàm updateOrder sau khi thanh toán thành công
            if (!paymentMethod.equalsIgnoreCase("VNPay")) {
                product.setQuantity(product.getQuantity() - cartItem.getQuantity());
                productRepo.save(product);
                cartService.deleteProductFromCart(cart.getCartId(), product.getProductId());
            }
        }

        savedOrder.setOrderItems(orderItems);
        orderRepo.save(savedOrder);

        OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
        orderDTO.setOrderItems(orderItems.stream()
                .map(item -> modelMapper.map(item, OrderItemDTO.class))
                .collect(Collectors.toList()));

        return orderDTO;
    }

    @Override
    @Transactional
    public OrderDTO updateOrder(String emailId, Long orderId, String orderStatus) {
        Order order = orderRepo.findOrderByEmailAndOrderId(emailId, orderId);
        if (order == null) throw new ResourceNotFoundException("Order", "id", orderId);

        // 🔥 LOGIC CHỐT ĐƠN: Khi chuyển từ PENDING sang ACCEPTED (VNPay thành công)
        if (order.getOrderStatus().equalsIgnoreCase("PENDING PAYMENT") && 
            orderStatus.equalsIgnoreCase("Order Accepted!")) {
            
            Cart cart = cartRepo.findCartByEmail(emailId);
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                
                // 1. Thực hiện trừ kho chính thức
                product.setQuantity(product.getQuantity() - item.getQuantity());
                productRepo.save(product);

                // 2. Thực hiện xóa sản phẩm này khỏi giỏ hàng
                if (cart != null) {
                    try {
                        cartService.deleteProductFromCart(cart.getCartId(), product.getProductId());
                    } catch (Exception e) { /* Bỏ qua lỗi item đã bị xóa */ }
                }
            }
        }

        order.setOrderStatus(orderStatus);
        return modelMapper.map(orderRepo.save(order), OrderDTO.class);
    }

    @Override
    public List<OrderDTO> getOrdersByUser(String emailId) {
        List<Order> orders = orderRepo.findAllByEmail(emailId);
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
}