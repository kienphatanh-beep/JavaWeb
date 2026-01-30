package com.example.didong2jv.security;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.example.didong2jv.service.impl.UserDetailsServiceImpl;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

@Component
public class JWTFilter extends OncePerRequestFilter {

    @Autowired 
    private JWTUtil jwtUtil;
    
    @Autowired 
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) 
            throws ServletException, IOException {
        
        // 🔥 FIX: Bỏ qua kiểm tra JWT cho các yêu cầu OPTIONS (CORS Preflight)
        // Yêu cầu này không chứa Token và cần được trả về 200 OK ngay lập tức.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        
        String authHeader = request.getHeader("Authorization");
        String uri = request.getRequestURI();

        // 1. Kiểm tra nếu không có Header Authorization hoặc không bắt đầu bằng Bearer
        if (authHeader == null || authHeader.isBlank() || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Nếu có Header, bắt đầu xử lý Token
        String jwt = authHeader.substring(7);
        
        try {
            if (!jwt.isBlank()) {
                String email = jwtUtil.validateTokenAndRetrieveSubject(jwt);
                
                if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(email);
                    
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            email, 
                            userDetails.getPassword(), 
                            userDetails.getAuthorities());
                    
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            System.err.println("--- [JWT FILTER ERROR] URI: " + uri + " | Lỗi: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}