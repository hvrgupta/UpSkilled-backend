package com.software.upskilled.dto;

import jakarta.annotation.Nullable;
import lombok.Data;

import java.util.List;

@Data
public class MessageRequestDTO
{
    /**
     * Data Transfer Object (DTO) for representing a message request.
     * This DTO is used to send a message from a sender to one or more receivers.
     */
    private Long courseId;
    private List<Long> receiverIds;
    private String message;
}
