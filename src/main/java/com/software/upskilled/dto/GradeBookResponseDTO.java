package com.software.upskilled.dto;

import lombok.Data;

import java.util.Date;

@Data
public class GradeBookResponseDTO
{
    private int grade;
    private String feedback;
    private Date gradedDate;
    private CreateUserDTO instructorDTO;
    private SubmissionResponseDTO submissionResponseDTO;
}
