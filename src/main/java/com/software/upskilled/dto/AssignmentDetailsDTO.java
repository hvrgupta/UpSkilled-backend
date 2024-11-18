package com.software.upskilled.dto;

import lombok.Data;

@Data
public class AssignmentDetailsDTO
{
    /**
     * Data Transfer Object (DTO) for holding assignment details.
     * This DTO is used to transfer assignment information between layers.
     */
    String title;
    String description;
    long deadline;
    long id;
}
