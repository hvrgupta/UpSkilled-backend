package com.software.upskilled.dto;

import jakarta.annotation.Nullable;
import lombok.Data;

import java.util.List;

@Data
public class MessageRequestDTO
{
    private Long courseId;
    private List<Long> receiverIds;
    private String message;
}
