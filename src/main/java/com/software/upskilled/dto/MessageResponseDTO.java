package com.software.upskilled.dto;

import lombok.Data;
import java.util.Date;

@Data
public class MessageResponseDTO
{
    /**
     * Data Transfer Object (DTO) for representing a response message.
     * This DTO is used to send back message details, including its content and metadata.
     */
    private Long messageId;
    private String message;
    private Date sentAt;
    private Boolean isRead;
}
