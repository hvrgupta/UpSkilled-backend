package com.software.upskilled.dto;

import lombok.Data;

@Data
public class ErrorResponseDTO
{
    private int httpCode;
    private String message;
}
