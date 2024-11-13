package com.software.upskilled.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AssignmentResponseDTO
{
    AssignmentDetailsDTO assignmentDetails;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<SubmissionResponseDTO> submissionDetails;

}
