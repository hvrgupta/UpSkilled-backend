package com.software.upskilled.dto;

import lombok.Data;

@Data
public class ErrorResponseDTO
{
    /**
     * Data Transfer Object (DTO) for error responses.
     * This DTO is used to structure error details returned to the client, such as HTTP status code and a message.
     */
    private int httpCode;
    private String message;
}
