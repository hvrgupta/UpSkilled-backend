package com.software.upskilled.utils;

import com.software.upskilled.dto.SuccessResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Utility class for creating standardized success response messages.
 * This class provides methods to generate consistent success responses
 * across the application.
 */
@Service
public class SucessResponseMessageUtil
{
    /**
     * Creates a standardized success response with an HTTP status code and message.
     *
     * @param httpCode the HTTP status code to be returned with the response.
     * @param successMessage the success message to be included in the response body.
     * @return a ResponseEntity containing the SuccessResponseDTO with the provided HTTP code
     *         and success message. Returns a 200 OK response if the HTTP code is 200,
     *         or the specified status code otherwise.
     */
    public ResponseEntity<SuccessResponseDTO> createSuccessResponseMessages(int httpCode, String successMessage ) {
        SuccessResponseDTO successResponseDTO = new SuccessResponseDTO();
        successResponseDTO.setHttpCode( httpCode );
        successResponseDTO.setMessage( successMessage );

        if( httpCode == 200 )
            return ResponseEntity.ok( successResponseDTO );
        else
            return ResponseEntity.status( httpCode ).body( successResponseDTO );
    }
}
