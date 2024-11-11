package com.software.upskilled.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.software.upskilled.Entity.Submission;
import lombok.Data;

import java.util.Date;

@Data
public class SubmissionResponseDTO
{
    private long submission_id;
    private String submission_url;
    private Date submission_at;
    private Submission.Status submission_status;
    private long assignmentID;
    private long gradeBookId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    GradeBookResponseDTO gradeBook;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    CreateUserDTO userDetails;
}
