package com.software.upskilled.utils;

import com.software.upskilled.dto.ErrorResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ErrorResponseMessageUtil
{
    public ResponseEntity<ErrorResponseDTO> createErrorResponseMessages( int errorCode, String errorMessage )
    {
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO();
        errorResponseDTO.setErrorCode(errorCode);
        errorResponseDTO.setErrorMessage(errorMessage);

        return ResponseEntity.status( errorCode ).body( errorResponseDTO );
    }
}
