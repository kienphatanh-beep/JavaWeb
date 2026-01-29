package com.example.didong2jv.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.didong2jv.entity.Cart;
import com.example.didong2jv.entity.CartItem;
import com.example.didong2jv.entity.Product;
import com.example.didong2jv.exceptions.APIException;
import com.example.didong2jv.exceptions.ResourceNotFoundException;
import com.example.didong2jv.payloads.CartDTO;
import com.example.didong2jv.payloads.ProductDTO;
import com.example.didong2jv.repository.CartItemRepo;
import com.example.didong2jv.repository.CartRepo;
import com.example.didong2jv.repository.ProductRepo;
import com.example.didong2jv.service.CartService;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class CartServiceImpl implements CartService {

    @Autowired private CartRepo cartRepo;
    @Autowired private ProductRepo productRepo;
    @Autowired private CartItemRepo cartItemRepo;
    @Autowired private ModelMapper modelMapper;

    // --- HÀM HỖ TRỢ BẢO MẬT: Lấy Email từ JWT Token ---
    private String getLoggedInUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName(); 
    }

    // =========================================================================
    // 🔥 CÁC HÀM DÀNH CHO USER (BẢO MẬT TUYỆT ĐỐI)
    // =========================================================================

    @Override
    public CartDTO getCartByLoggedInUser() {
        String email = getLoggedInUserEmail();
        Cart cart = cartRepo.findCartByEmail(email);
        if (cart == null) throw new ResourceNotFoundException("Cart", "email", email);
        return mapCartToDTO(cart);
    }

    @Override
    public CartDTO addProductToCartForUser(Long productId, Integer quantity) {
        String email = getLoggedInUserEmail();
        Cart cart = cartRepo.findCartByEmail(email);
        if (cart == null) throw new ResourceNotFoundException("Cart", "email", email);
        return addProductToCart(cart.getCartId(), productId, quantity);
    }

    @Override
    public CartDTO updateProductQuantityInCartForUser(Long productId, Integer quantity) {
        String email = getLoggedInUserEmail();
        Cart cart = cartRepo.findCartByEmail(email);
        if (cart == null) throw new ResourceNotFoundException("Cart", "email", email);
        return updateProductQuantityInCart(cart.getCartId(), productId, quantity);
    }

    @Override
    public String deleteProductFromCartForUser(Long productId) {
        String email = getLoggedInUserEmail();
        Cart cart = cartRepo.findCartByEmail(email);
        if (cart == null) throw new ResourceNotFoundException("Cart", "email", email);
        return deleteProductFromCart(cart.getCartId(), productId);
    }

    // =========================================================================
    // 🔥 CÁC HÀM HỆ THỐNG & ADMIN (FIX LỖI UNDEFINED)
    // =========================================================================

    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        Cart cart = cartRepo.findById(cartId).orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
        Product product = productRepo.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        CartItem cartItem = cartItemRepo.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) return;

        double cartPrice = cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity());
        cartItem.setProductPrice(product.getSpecialPrice());
        cartItem.setDiscount(product.getDiscount());
        cart.setTotalPrice(cartPrice + (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItemRepo.save(cartItem);
        cartRepo.save(cart);
    }

    @Override
    public CartDTO addProductToCart(Long cartId, Long productId, Integer quantity) {
        Cart cart = cartRepo.findById(cartId).orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
        Product product = productRepo.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        CartItem cartItem = cartItemRepo.findCartItemByProductIdAndCartId(cartId, productId);

        if (product.getQuantity() < quantity) throw new APIException("Quantity not enough");

        if (cartItem != null) {
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
        } else {
            cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setCart(cart);
            cartItem.setQuantity(quantity);
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setDiscount(product.getDiscount());
        }
        cartItemRepo.save(cartItem);
        product.setQuantity(product.getQuantity() - quantity);
        productRepo.save(product);
        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));
        cartRepo.save(cart);
        return mapCartToDTO(cart);
    }

    @Override
    public CartDTO updateProductQuantityInCart(Long cartId, Long productId, Integer quantity) {
        Cart cart = cartRepo.findById(cartId).orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
        Product product = productRepo.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        CartItem cartItem = cartItemRepo.findCartItemByProductIdAndCartId(cartId, productId);
        if (cartItem == null) throw new APIException("Product not in cart");

        int diff = quantity - cartItem.getQuantity();
        if (diff > 0 && product.getQuantity() < diff) throw new APIException("Inventory not enough");

        product.setQuantity(product.getQuantity() - diff);
        productRepo.save(product);

        cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * diff));
        cartItem.setQuantity(quantity);
        cartItemRepo.save(cartItem);
        cartRepo.save(cart);
        return mapCartToDTO(cart);
    }

    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepo.findById(cartId).orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
        CartItem cartItem = cartItemRepo.findCartItemByProductIdAndCartId(cartId, productId);
        if (cartItem == null) throw new ResourceNotFoundException("Product", "productId", productId);

        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));
        Product product = cartItem.getProduct();
        product.setQuantity(product.getQuantity() + cartItem.getQuantity());
        productRepo.save(product);

        cartItemRepo.deleteCartItemByProductIdAndCartId(productId, cartId);
        cartRepo.save(cart);
        return "Product removed from cart";
    }

    @Override
    public List<CartDTO> getAllCarts() {
        return cartRepo.findAll().stream().map(this::mapCartToDTO).collect(Collectors.toList());
    }

    @Override
    public CartDTO getCart(String emailId, Long cartId) {
        Cart cart = cartRepo.findCartByEmailAndCartId(emailId, cartId);
        if (cart == null) throw new ResourceNotFoundException("Cart", "cartId", cartId);
        return mapCartToDTO(cart);
    }

    private CartDTO mapCartToDTO(Cart cart) {
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<ProductDTO> products = cart.getCartItems().stream().map(item -> {
            ProductDTO dto = modelMapper.map(item.getProduct(), ProductDTO.class);
            dto.setQuantity(item.getQuantity()); // Ghi đè số lượng mua
            dto.setSpecialPrice(item.getProductPrice()); // Ghi đè giá lúc mua
            return dto;
        }).collect(Collectors.toList());
        cartDTO.setProducts(products);
        return cartDTO;
    }
}