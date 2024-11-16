package com.software.upskilled.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CourseMessagesResponseDTO
{
    Map<String,String> user;
    @JsonInclude( JsonInclude.Include.NON_NULL )
    List<MessageResponseDTO> messages;
}
