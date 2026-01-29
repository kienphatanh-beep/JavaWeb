package com.example.didong2jv.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.didong2jv.payloads.CartDTO;
import com.example.didong2jv.service.CartService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@RequestMapping("/api")
@SecurityRequirement(name = "E-Commerce Application")
public class CartController {

    @Autowired
    private CartService cartService;

    // --- API DÀNH CHO USER (Được bảo mật bằng Token) ---

    @GetMapping("/carts")
    public ResponseEntity<CartDTO> getCart() {
        CartDTO cartDTO = cartService.getCartByLoggedInUser();
        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }

    @PostMapping("/carts/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(@PathVariable Long productId, @PathVariable Integer quantity) {
        CartDTO cartDTO = cartService.addProductToCartForUser(productId, quantity);
        return new ResponseEntity<>(cartDTO, HttpStatus.CREATED);
    }

    @PutMapping("/carts/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> updateCartProduct(@PathVariable Long productId, @PathVariable Integer quantity) {
        CartDTO cartDTO = cartService.updateProductQuantityInCartForUser(productId, quantity);
        return new ResponseEntity<>(cartDTO, HttpStatus.OK);
    }

    @DeleteMapping("/carts/product/{productId}")
    public ResponseEntity<String> deleteProductFromCart(@PathVariable Long productId) {
        String status = cartService.deleteProductFromCartForUser(productId);
        return new ResponseEntity<>(status, HttpStatus.OK);
    }

    // --- API DÀNH CHO ADMIN ---

    @GetMapping("/admin/carts")
    public ResponseEntity<List<CartDTO>> getCarts() {
        List<CartDTO> cartDTOs = cartService.getAllCarts();
        return new ResponseEntity<>(cartDTOs, HttpStatus.OK);
    }
}