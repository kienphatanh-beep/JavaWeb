package com.example.didong2jv.config; // Đã cập nhật theo yêu cầu đường dẫn của bạn

import org.springframework.context.annotation.Configuration; //
import org.springframework.http.MediaType; //
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer; //
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer; //

@Configuration //
public class ContentConfig implements WebMvcConfigurer { //

    @Override //
    public void configureContentNegotiation(@SuppressWarnings("null") ContentNegotiationConfigurer configurer) { //
        
        // Cấu hình để API có thể trả về các định dạng dữ liệu khác nhau (JSON hoặc XML)
        configurer.favorParameter(true) // Cho phép xác định định dạng qua tham số trên URL
                  .parameterName("mediaType") // Tên tham số sử dụng là ?mediaType=
                  .defaultContentType(MediaType.APPLICATION_JSON) // Mặc định trả về JSON nếu không chỉ định
                  .mediaType("json", MediaType.APPLICATION_JSON) // Bản đồ hóa giá trị "json" thành định dạng JSON
                  .mediaType("xml", MediaType.APPLICATION_XML); // Bản đồ hóa giá trị "xml" thành định dạng XML
    }
}