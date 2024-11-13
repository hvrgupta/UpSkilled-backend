package com.software.upskilled.utils;

import com.software.upskilled.dto.ErrorResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ErrorResponseMessageUtil
{
    public ResponseEntity<ErrorResponseDTO> createErrorResponseMessages( int httpCode, String errorMessage )
    {
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO();
        errorResponseDTO.setHttpCode(httpCode);
        errorResponseDTO.setErrorMessage(errorMessage);

        return ResponseEntity.status( httpCode ).body( errorResponseDTO );
    }
}
