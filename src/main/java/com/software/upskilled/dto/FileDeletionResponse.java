package com.software.upskilled.dto;

import lombok.Data;

@Data
public class FileDeletionResponse
{
    private String fileName;
    private boolean isDeletionSuccessfull;
}
