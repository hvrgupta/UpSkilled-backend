package com.software.upskilled.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileUploadResponse {
        /**
         * Data Transfer Object (DTO) for the response of file upload operations.
         * This DTO is used to structure the result of a file upload request,
         * including the path where the file is stored and the timestamp of the upload.
         */
        private String filePath;
        private LocalDateTime dateTime;
}
