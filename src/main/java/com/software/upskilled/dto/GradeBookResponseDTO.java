package com.software.upskilled.dto;

import lombok.Data;

import java.util.Date;

@Data
public class GradeBookResponseDTO
{
    private int grade;
    private String feedback;
    private Date gradedDate;
    private long instructorID;
    private long submissionID;
}
