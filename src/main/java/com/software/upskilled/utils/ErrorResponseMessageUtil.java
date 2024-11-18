package com.software.upskilled.utils;

import com.software.upskilled.dto.ErrorResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Service class for creating standardized error response messages.
 * This class provides utility methods to generate consistent error responses
 * across the application.
 */
@Service
public class ErrorResponseMessageUtil
{
    public ResponseEntity<ErrorResponseDTO> createErrorResponseMessages( int httpCode, String errorMessage ) {
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO();
        errorResponseDTO.setHttpCode(httpCode);
        errorResponseDTO.setMessage(errorMessage);
        return ResponseEntity.status( httpCode ).body( errorResponseDTO );
    }
}
