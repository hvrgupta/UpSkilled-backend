package com.software.upskilled.dto;

import lombok.Data;

@Data
public class SuccessResponseDTO
{
    /**
     * Data Transfer Object (DTO) for representing a successful response.
     * This DTO is used to send a success message along with the HTTP status code
     * indicating the success of an operation.
     */
    private int httpCode;
    private String message;
}
