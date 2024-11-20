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
    /**
     * Creates an error response message with the specified HTTP status code and error message.
     *
     * This method constructs an `ErrorResponseDTO` object containing the provided HTTP status code and error message,
     * and then returns a `ResponseEntity` with the appropriate HTTP status and the error response body.
     *
     * @param httpCode The HTTP status code to be returned in the response.
     * @param errorMessage The error message to be included in the response body.
     * @return A `ResponseEntity` containing the error response body and the corresponding HTTP status code.
     */
    public ResponseEntity<ErrorResponseDTO> createErrorResponseMessages( int httpCode, String errorMessage ) {
        ErrorResponseDTO errorResponseDTO = new ErrorResponseDTO();
        errorResponseDTO.setHttpCode(httpCode);
        errorResponseDTO.setMessage(errorMessage);
        return ResponseEntity.status( httpCode ).body( errorResponseDTO );
    }
}
