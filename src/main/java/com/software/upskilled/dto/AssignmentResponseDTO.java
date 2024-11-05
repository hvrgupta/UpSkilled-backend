package com.software.upskilled.dto;

import lombok.Data;

import java.util.Date;

@Data
public class AssignmentResponseDTO
{
    long assignmentID;
    String assignmentTitle;
    String assignmentDescription;
    long assignmentDeadline;
}
