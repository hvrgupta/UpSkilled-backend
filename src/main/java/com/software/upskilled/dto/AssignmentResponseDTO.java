package com.software.upskilled.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class AssignmentResponseDTO
{
    /**
     * Data Transfer Object (DTO) for holding the response of an assignment.
     * This DTO is used to transfer assignment details along with any submission data.
     */
    AssignmentDetailsDTO assignmentDetails;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<SubmissionResponseDTO> submissionDetails;
}
