package com.software.upskilled.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.software.upskilled.Entity.Submission;
import lombok.Data;

import java.util.Date;

@Data
public class SubmissionResponseDTO
{
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
