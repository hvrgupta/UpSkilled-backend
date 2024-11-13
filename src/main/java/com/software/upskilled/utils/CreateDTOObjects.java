package com.software.upskilled.utils;

import com.software.upskilled.Entity.Assignment;
import com.software.upskilled.Entity.Gradebook;
import com.software.upskilled.Entity.Submission;
import com.software.upskilled.Entity.Users;
import com.software.upskilled.dto.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface CreateDTOObjects
{
    public CreateUserDTO createUserDTO( Users userDetails );

    public SubmissionResponseDTO createSubmissionDTO(Submission submission, Assignment assignment, Users userDetails );

    public GradeBookResponseDTO  createGradeBookResponseDTO( Gradebook gradebook, Long instructorID, Long submissionId );

    public AssignmentResponseDTO createAssignmentResponseDTO(AssignmentDetailsDTO assignmentDetailsDTO, List<SubmissionResponseDTO> submissionResponseDTOList);

}
