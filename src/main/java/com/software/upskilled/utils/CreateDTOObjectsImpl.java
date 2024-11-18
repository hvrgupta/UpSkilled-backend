package com.software.upskilled.utils;

import com.software.upskilled.Entity.*;
import com.software.upskilled.dto.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Implementation class for creating Data Transfer Objects (DTOs).
 * This class is responsible for instantiating and populating DTO objects
 * based on business logic and data from various sources.
 */
@Component
public class CreateDTOObjectsImpl implements CreateDTOObjects
{
    @Override
    public CreateUserDTO createUserDTO(Users userObject) {
        //Creating the User DTO Object
        CreateUserDTO createUserDTO = new CreateUserDTO();
        //Setting the various properties of the DTO Object
        createUserDTO.setFirstName( userObject.getFirstName() );
        createUserDTO.setLastName( userObject.getLastName() );
        createUserDTO.setEmail( userObject.getEmail() );
        createUserDTO.setDesignation( userObject.getDesignation() );
        createUserDTO.setId( userObject.getId() );
        createUserDTO.setRole( userObject.getRole() );
        return createUserDTO;
    }

    @Override
    public SubmissionResponseDTO createSubmissionDTO(Submission submission, Assignment assignment, Users userDetails) {
        //Create the Submission Response DTO Object
        SubmissionResponseDTO submissionResponseDTO = new SubmissionResponseDTO();

        submissionResponseDTO.setSubmissionId( submission.getId() );
        submissionResponseDTO.setSubmissionUrl( submission.getSubmissionUrl() );
        submissionResponseDTO.setSubmissionAt( submission.getSubmittedAt() );
        submissionResponseDTO.setSubmissionStatus( submission.getStatus() );
        submissionResponseDTO.setAssignmentID(assignment.getId());

        //Create the CreateUser DTO Object
        CreateUserDTO userDTOObject =  createUserDTO( userDetails );

        //Add the UserDTO to the SubmissionResponse
        submissionResponseDTO.setUserDetails( userDTOObject );

        //check if the submission response has a GradeBook. If it has GradeBook then set the details, else set it null
        if( submission.getStatus().equals( Submission.Status.GRADED ) )
        {
            //Get the ID of the instructor for the submission
            long instructorID = assignment.getCreatedBy().getId();
            //Get the submissionID
            long submissionID = submission.getId();

            //Fetch the GradeBook object
            Gradebook gradebook = submission.getGrade();
            //Pass the details to the createGradeBookResponseObject and get the GradeBook object
            GradeBookResponseDTO gradeBookResponseDTOObject = createGradeBookResponseDTO( gradebook, instructorID, submissionID );
            //Set the GradeBook Response Objects in the DTO
            submissionResponseDTO.setGradeBook( gradeBookResponseDTOObject );
        }
        else
            submissionResponseDTO.setGradeBook( null );

        //Return the submission dto object
        return submissionResponseDTO;
    }

    @Override
    public GradeBookResponseDTO createGradeBookResponseDTO(Gradebook gradebook, Long instructorID, Long submissionId) {

        //Creating  the GradeBook DTO object to populate the grades
        GradeBookResponseDTO gradeBookResponseDTO = new GradeBookResponseDTO();

        //Setting the details of the object
        gradeBookResponseDTO.setGrade( gradebook.getGrade() );
        gradeBookResponseDTO.setGradeBookId( gradebook.getId() );
        gradeBookResponseDTO.setFeedback(gradebook.getFeedback() );
        gradeBookResponseDTO.setSubmissionID( submissionId );
        gradeBookResponseDTO.setInstructorID( instructorID );
        gradeBookResponseDTO.setGradedDate( gradebook.getGradedAt() );

        //Return the details
        return gradeBookResponseDTO;
    }

    @Override
    public AssignmentResponseDTO createAssignmentResponseDTO(AssignmentDetailsDTO assignmentDetailsDTO, List<SubmissionResponseDTO> submissionResponseDTOList) {

        //Creating the Assignment Response DTO Object
        AssignmentResponseDTO assignmentResponseDTO = new AssignmentResponseDTO();
        //Setting the values of the DTO
        assignmentResponseDTO.setAssignmentDetails( assignmentDetailsDTO );
        //Setting the values of the submissionDetails
        assignmentResponseDTO.setSubmissionDetails( submissionResponseDTOList );

        //Return the Response
        return assignmentResponseDTO;
    }

    @Override
    public MessageResponseDTO createMessageResponseDTO(Message messageDetails) {
        //Creating the MessageResponse DTO Object
        MessageResponseDTO messageResponseDTO = new MessageResponseDTO();

        //Setting the details
        messageResponseDTO.setMessageId( messageDetails.getId() );
        messageResponseDTO.setMessage( messageDetails.getContent() );
        messageResponseDTO.setIsRead( messageDetails.getIsRead() );
        messageResponseDTO.setSentAt( messageDetails.getSentAt() );

        //Returning the DTO object
        return   messageResponseDTO;
    }

    @Override
    public CourseMessagesResponseDTO createCourseMessagesResponseDTO(Map<String, String> userDetails, List<Message> messages) {
        //Create the CourseMessageResponseDTO object
        CourseMessagesResponseDTO courseMessagesResponseDTO = new CourseMessagesResponseDTO();

        //Set the userDetails
        courseMessagesResponseDTO.setUser( userDetails );

        //Iterate over the messages and create the List of the MessageResponseDTO
        List<MessageResponseDTO> messageResponseDTOList = messages.stream().map(this::createMessageResponseDTO).toList();

        //Set the messageResponseDTO list
        courseMessagesResponseDTO.setMessages( messageResponseDTOList );

        //Return the created DTO object
        return courseMessagesResponseDTO;

    }
}
