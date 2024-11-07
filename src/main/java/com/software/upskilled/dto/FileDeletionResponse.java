package com.software.upskilled.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class FileDeletionResponse
{
    private String fileName;
    private boolean isDeletionSuccessfull;
}
