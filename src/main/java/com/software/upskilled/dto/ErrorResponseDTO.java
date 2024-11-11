package com.software.upskilled.dto;

import lombok.Data;

@Data
public class ErrorResponseDTO
{
    private int errorCode;
    private String errorMessage;
}
