package com.software.upskilled.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AssignmentResponseDTO
{
    long id;
    String title;
    String description;
    long deadline;
    int grade;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<SubmissionResponseDTO> submissionDetails;

}
