package com.software.upskilled.utils;

import com.software.upskilled.Entity.*;
import com.software.upskilled.dto.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public interface CreateDTOObjects
{
    public CreateUserDTO createUserDTO( Users userDetails );

    public SubmissionResponseDTO createSubmissionDTO(Submission submission, Assignment assignment, Users userDetails );

    public GradeBookResponseDTO  createGradeBookResponseDTO( Gradebook gradebook, Long instructorID, Long submissionId );

    public AssignmentResponseDTO createAssignmentResponseDTO(AssignmentDetailsDTO assignmentDetailsDTO, List<SubmissionResponseDTO> submissionResponseDTOList);

    public MessageResponseDTO createMessageResponseDTO( Message messageDetails );

    public CourseMessagesResponseDTO createCourseMessagesResponseDTO(Map<String,String> userDetails, List<Message> messages);

}
