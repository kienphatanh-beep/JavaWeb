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
        
        String authHeader = request.getHeader("Authorization");
        String uri = request.getRequestURI();

        // 1. Kiểm tra nếu không có Header Authorization hoặc không bắt đầu bằng Bearer
        // Thì cho đi tiếp luôn (không chặn bằng sendError)
        if (authHeader == null || authHeader.isBlank() || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Nếu có Header, bắt đầu xử lý Token
        String jwt = authHeader.substring(7);
        
        try {
            if (!jwt.isBlank()) {
                // Lấy Email từ Token
                String email = jwtUtil.validateTokenAndRetrieveSubject(jwt);
                
                // Nếu Token hợp lệ và chưa được xác thực trong Context
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
            // 🔥 QUAN TRỌNG: Nếu Token lỗi (hết hạn, sai định dạng) trên trang Public, 
            // chúng ta chỉ log lại chứ không dùng response.sendError ở đây.
            // Để SecurityFilterChain ở dưới tự chặn nếu API đó yêu cầu Auth.
            System.err.println("--- [JWT FILTER ERROR] URI: " + uri + " | Lỗi: " + e.getMessage());
        }

        // Luôn luôn gọi doFilter để request được tiếp tục đi vào chuỗi Filter tiếp theo
        filterChain.doFilter(request, response);
    }
}