package com.example.didong2jv.security;

import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

@Component
public class JWTUtil {

    @Value("${jwt_secret}")
    private String secret;

    // Thời gian hết hạn: 24 tiếng (cho khớp với cấu hình AppConstants của bạn)
    private static final long EXPIRE_DURATION = 24 * 60 * 60 * 1000; 

    public String generateToken(String email) {
        return JWT.create()
                // QUAN TRỌNG: Đổi Subject thành Email (thay vì chuỗi cố định "User Details")
                .withSubject(email) 
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRE_DURATION))
                .withIssuer("DiDongApp")
                .sign(Algorithm.HMAC256(secret));
    }

    public String validateTokenAndRetrieveSubject(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                // BỎ dòng .withSubject("User Details") đi vì Subject giờ là email thay đổi theo từng người
                .withIssuer("DiDongApp")
                .build();
        
        DecodedJWT jwt = verifier.verify(token);
        
        // Lấy Subject (chính là Email) ra để trả về
        return jwt.getSubject();
    }
    
}