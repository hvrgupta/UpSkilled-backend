package com.software.upskilled.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class FileDeletionResponse
{
    /**
     * Data Transfer Object (DTO) for the response of file deletion operations.
     * This DTO is used to structure the result of a file deletion request.
     */
    private String fileName;
    private boolean isDeletionSuccessfull;
}
