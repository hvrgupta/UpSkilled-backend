package com.software.upskilled.dto;

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
    private AssignmentResponseDTO assignmentResponseDTO;
}
