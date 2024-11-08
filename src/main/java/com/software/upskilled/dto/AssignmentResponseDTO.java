package com.software.upskilled.dto;

import lombok.Data;

import java.util.Date;

@Data
public class AssignmentResponseDTO
{
    long id;
    String title;
    String description;
    long deadline;
}
