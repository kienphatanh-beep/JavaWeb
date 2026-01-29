package com.example.didong2jv.exceptions; //

import java.util.HashMap; //
import java.util.Map; //

import org.springframework.dao.DataIntegrityViolationException; //
import org.springframework.http.HttpStatus; //
import org.springframework.http.ResponseEntity; //
import org.springframework.security.core.AuthenticationException; //
import org.springframework.validation.FieldError; //
import org.springframework.web.bind.MethodArgumentNotValidException; //
import org.springframework.web.bind.MissingPathVariableException; //
import org.springframework.web.bind.annotation.ExceptionHandler; //
import org.springframework.web.bind.annotation.RestControllerAdvice; //

import com.example.didong2jv.payloads.APIResponse; // Giả sử bạn có class APIResponse trong package payloads
import jakarta.validation.ConstraintViolationException; //

@RestControllerAdvice //
public class MyGlobalExceptionHandler {

    // Xử lý lỗi khi không tìm thấy tài nguyên (ví dụ: Product không tồn tại)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIResponse> myResourceNotFoundException(ResourceNotFoundException e) {
        String message = e.getMessage(); //
        APIResponse res = new APIResponse(message, false); //
        return new ResponseEntity<APIResponse>(res, HttpStatus.NOT_FOUND); //
    }

    // Xử lý các lỗi API tùy chỉnh
    @ExceptionHandler(APIException.class)
    public ResponseEntity<APIResponse> myAPIException(APIException e) {
        String message = e.getMessage(); //
        APIResponse res = new APIResponse(message, false); //
        return new ResponseEntity<APIResponse>(res, HttpStatus.BAD_REQUEST); //
    }

    // Xử lý lỗi validate dữ liệu (ví dụ: tên quá ngắn, email sai định dạng)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> myMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> res = new HashMap<>(); //
        e.getBindingResult().getAllErrors().forEach(err -> { //
            String fieldName = ((FieldError) err).getField(); //
            String message = err.getDefaultMessage(); //
            res.put(fieldName, message); //
        });
        return new ResponseEntity<Map<String, String>>(res, HttpStatus.BAD_REQUEST); //
    }

    // Xử lý lỗi xác thực người dùng
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> myAuthenticationException(AuthenticationException e) {
        String res = e.getMessage(); //
        return new ResponseEntity<String>(res, HttpStatus.BAD_REQUEST); //
    }
}