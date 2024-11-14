package com.software.upskilled.utils;

import com.software.upskilled.dto.ErrorResponseDTO;
import com.software.upskilled.dto.SuccessResponseDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class SucessResponseMessageUtil
{
    public ResponseEntity<SuccessResponseDTO> createSuccessResponseMessages(int httpCode, String successMessage )
    {
        SuccessResponseDTO successResponseDTO = new SuccessResponseDTO();
        successResponseDTO.setHttpCode( httpCode );
        successResponseDTO.setMessage( successMessage );

        if( httpCode == 200 )
            return ResponseEntity.ok( successResponseDTO );
        else
            return ResponseEntity.status( httpCode ).body( successResponseDTO );
    }
}
