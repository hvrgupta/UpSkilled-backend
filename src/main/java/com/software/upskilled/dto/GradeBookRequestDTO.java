package com.software.upskilled.dto;

import lombok.Data;

@Data
public class GradeBookRequestDTO
{
    /**
     * Data Transfer Object (DTO) for submitting grades and feedback for assignments.
     * This DTO is used when a grade and feedback are provided for a specific assignment submission.
     */
    private Integer grade;
    private String feedback;
}
