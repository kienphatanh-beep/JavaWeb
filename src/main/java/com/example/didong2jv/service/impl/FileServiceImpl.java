package com.example.didong2jv.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.didong2jv.service.FileService;

@Service
public class FileServiceImpl implements FileService {

    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {
        // Lấy tên file gốc
        String originalFileName = file.getOriginalFilename();
        
        // Tạo tên file ngẫu nhiên để tránh trùng lặp (ví dụ: a1b2c3d4.png)
        String randomId = UUID.randomUUID().toString();
        String fileName = randomId.concat(originalFileName.substring(originalFileName.lastIndexOf(".")));

        // Tạo đường dẫn đầy đủ
        String filePath = path + File.separator + fileName;

        // Tạo thư mục nếu chưa tồn tại
        File f = new File(path);
        if (!f.exists()) {
            f.mkdir();
        }

        // Copy file ảnh vào thư mục
        Files.copy(file.getInputStream(), Paths.get(filePath));

        return fileName;
    }

    @Override
    public InputStream getResource(String path, String fileName) throws FileNotFoundException {
        String filePath = path + File.separator + fileName;
        return new FileInputStream(filePath);
    }
}