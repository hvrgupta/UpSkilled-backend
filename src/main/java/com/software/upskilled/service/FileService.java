package com.software.upskilled.service;

import com.software.upskilled.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    FileUploadResponse uploadSyllabus(MultipartFile multipartFile, Long courseId);
    public byte[] viewSyllabus(Long courseId);
}