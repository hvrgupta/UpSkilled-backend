package com.software.upskilled.dto;

import lombok.Data;
import java.util.Date;

@Data
public class MessageResponseDTO
{
    private Long messageId;
    private String message;
    private Date sentAt;
    private Boolean isRead;
}
