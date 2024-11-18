package com.software.upskilled.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CourseMessagesResponseDTO
{
    /**
     * Data Transfer Object (DTO) for representing the course-related messages and associated user information.
     * This DTO is used to transfer a map of user details and a list of messages associated with a specific course.
     */
    Map<String,String> user;
    @JsonInclude( JsonInclude.Include.NON_NULL )
    List<MessageResponseDTO> messages;
}
