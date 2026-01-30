package com.example.didong2jv.config;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.example.didong2jv.security.JWTFilter;
import com.example.didong2jv.service.impl.UserDetailsServiceImpl;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JWTFilter jwtFilter;

    @Autowired
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            
            .authorizeHttpRequests(requests -> requests
                // 1. CHO PHÉP OPTIONS (PREFLIGHT) - Dứt điểm lỗi CORS 403
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // 2. CHO PHÉP SWAGGER & OPENAPI
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/swagger-resources/**", "/webjars/**").permitAll()

                // 3. API Công khai & ChatAI
                .requestMatchers("/api/login", "/api/register", "/api/public/**", "/error").permitAll()
                .requestMatchers("/api/chat/**").permitAll() 
                .requestMatchers(HttpMethod.GET, "/api/categories/**", "/api/products/**").permitAll()
                
                // 4. Phân quyền User & Admin
                .requestMatchers("/api/carts/**").hasAuthority("USER")
                .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/categories/**", "/api/products/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/categories/**", "/api/products/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/categories/**", "/api/products/**").hasAuthority("ADMIN")

                .anyRequest().authenticated()
            )
            
            .exceptionHandling(handling -> handling.authenticationEntryPoint(
                (request, response, authException) -> 
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error: Unauthorized")
            ))
            
            .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        http.authenticationProvider(daoAuthenticationProvider());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 🔥 Cấu hình các cổng được phép truy cập
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000", 
            "http://127.0.0.1:3000",
            "http://localhost:8081", 
            "http://127.0.0.1:8081"
        )); 

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        configuration.setExposedHeaders(Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials", "Authorization", "X-Total-Count", "Content-Range"));
        configuration.setAllowCredentials(true); 
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsServiceImpl);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}