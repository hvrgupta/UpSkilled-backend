package com.software.upskilled.dto;

import lombok.Data;

import java.util.Date;

@Data
public class GradeBookResponseDTO
{
    /**
     * Data Transfer Object (DTO) for representing the response after a grade is assigned to an assignment submission.
     * This DTO is returned to the client after a grade is recorded for an assignment.
     */
    private int grade;
    private long gradeBookId;
    private String feedback;
    private Date gradedDate;
    private long instructorID;
    private long submissionID;
}
