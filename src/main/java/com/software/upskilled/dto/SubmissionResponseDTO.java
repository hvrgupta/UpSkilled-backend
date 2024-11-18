package com.software.upskilled.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.software.upskilled.Entity.Submission;
import lombok.Data;

import java.util.Date;

@Data
public class SubmissionResponseDTO
{
    /**
     * Data Transfer Object (DTO) for representing the response of a submission.
     * This DTO is used to send back the details of a submission, including its status,
     * associated assignment, grade, and user details.
     */
    private long submissionId;
    private String submissionUrl;
    private Date submissionAt;
    private Submission.Status submissionStatus;
    private long assignmentID;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    GradeBookResponseDTO gradeBook;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    CreateUserDTO userDetails;
}
