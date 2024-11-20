package com.software.upskilled.utils;

import com.software.upskilled.Entity.*;
import com.software.upskilled.dto.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Interface for creating various DTO (Data Transfer Object) representations for different entities.
 * Provides methods to convert domain objects like `Users`, `Submission`, `Gradebook`, `Assignment`, `Message`, and `CourseMessages`
 * into corresponding response DTOs for use in the application's API layer.
 * Each method takes relevant domain objects and returns the corresponding DTO.
 */
@Component
public interface CreateDTOObjects
{
    public CreateUserDTO createUserDTO( Users userDetails );

    public SubmissionResponseDTO createSubmissionDTO(Submission submission, Assignment assignment, Users userDetails );

    public GradeBookResponseDTO  createGradeBookResponseDTO( Gradebook gradebook, Long instructorID, Long submissionId );

    public AssignmentResponseDTO createAssignmentResponseDTO(AssignmentDetailsDTO assignmentDetailsDTO, List<SubmissionResponseDTO> submissionResponseDTOList);

    public MessageResponseDTO createMessageResponseDTO( Message messageDetails );

    public CourseMessagesResponseDTO createCourseMessagesResponseDTO(Map<String,String> userDetails, List<Message> messages);

    public AssignmentDetailsDTO createAssignmentDetailsDTO(Assignment assignmentDetails);

}
