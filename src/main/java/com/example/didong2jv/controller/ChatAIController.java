package com.example.didong2jv.controller;

import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatAIController {

    @PostMapping(value = "/ask", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> askAI(
            @RequestParam(value = "message", required = false) String message,
            @RequestParam(value = "history", required = false) String history, // Nhận mảng history dạng String JSON
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestHeader(value = "Authorization", required = false) String token) {

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA); // Giữ nguyên FormData để ổn định
            if (token != null) headers.set("Authorization", token);

            // Đóng gói dữ liệu gửi sang Flask
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("message", message);
            if (history != null) body.add("history", history); // Gửi history dạng chuỗi sang Flask
            if (image != null && !image.isEmpty()) {
                body.add("image", image.getResource());
            }

            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
            
            // Chuyển tiếp sang Flask AI Server
            String flaskResponse = restTemplate.postForObject("http://127.0.0.1:5000/ask", entity, String.class);
            return ResponseEntity.ok(flaskResponse);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body("{\"reply\": \"AI đang bận, bạn thử lại sau nhé!\"}");
        }
    }
}