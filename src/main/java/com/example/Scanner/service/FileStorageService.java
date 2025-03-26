package com.example.Scanner.service;

import com.example.Scanner.model.FileEntity;
import com.example.Scanner.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileStorageService {
    private final FileRepository fileRepository;

    public FileEntity storeFile(MultipartFile file) throws IOException {
        // Нормализация имени файла
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        if (fileName.contains("..")) {
            throw new IllegalArgumentException("Sorry! Filename contains invalid path sequence " + fileName);
        }

        // Создание новой сущности файла
        FileEntity fileEntity = new FileEntity();
        fileEntity.setName(fileName);
        fileEntity.setType(file.getContentType());
        fileEntity.setData(file.getBytes());
        fileEntity.setSignature(calculateSignature(file.getBytes()));

        // Сохранение в БД
        return fileRepository.save(fileEntity);
    }

    public FileEntity getFile(Long fileId) {
        return fileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with id " + fileId));
    }

    public FileEntity getFileByName(String name) {
        return fileRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("File not found with name " + name));
    }

    public List<FileEntity> getAllFiles() {
        return fileRepository.findAll();
    }

    private String calculateSignature(byte[] fileData) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(fileData);
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error calculating file signature", e);
        }
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
