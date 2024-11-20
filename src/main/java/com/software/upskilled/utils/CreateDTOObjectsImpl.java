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
    /**
     * Converts a user object to a CreateUserDTO object.
     *
     * This method takes a `Users` entity object and maps its properties (such as first name, last name, email, designation,
     * ID, and role) to a `CreateUserDTO` object. The resulting DTO can be used for data transfer or for creating a user in
     * an API response.
     *
     * @param userObject The `Users` entity object to be converted into a `CreateUserDTO`.
     * @return A `CreateUserDTO` object populated with data from the provided `userObject`.
     */
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

    /**
     * Converts a Submission entity into a SubmissionResponseDTO for data transfer.
     *
     * This method creates a `SubmissionResponseDTO` by extracting relevant information from the provided `Submission`,
     * `Assignment`, and `Users` entities. It also checks if the submission has been graded and includes the grade details
     * in the response if available. The DTO object encapsulates the submission details, assignment information, user details,
     * and gradebook data (if applicable).
     *
     * @param submission The `Submission` entity that contains the submission details.
     * @param assignment The `Assignment` entity related to the submission.
     * @param userDetails The `Users` entity representing the user who submitted the assignment.
     * @return A `SubmissionResponseDTO` containing submission details, user information, and gradebook (if graded).
     */
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

    /**
     * Converts a Gradebook entity into a GradeBookResponseDTO for data transfer.
     *
     * This method creates a `GradeBookResponseDTO` by extracting relevant information from the provided `Gradebook` entity,
     * including the grade, feedback, and grading date. It also includes the instructor ID and submission ID associated with the
     * grade entry. The resulting DTO is used to transfer the grade details to the client.
     *
     * @param gradebook The `Gradebook` entity containing the grading details.
     * @param instructorID The ID of the instructor who graded the submission.
     * @param submissionId The ID of the submission being graded.
     * @return A `GradeBookResponseDTO` containing the grading information, feedback, and related submission/instructor details.
     */
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

    /**
     * Converts assignment and submission data into an AssignmentResponseDTO for data transfer.
     *
     * This method creates an `AssignmentResponseDTO` by populating it with the details of an assignment and its corresponding
     * submissions. It accepts an `AssignmentDetailsDTO` object containing the assignment information and a list of
     * `SubmissionResponseDTO` objects representing the details of submissions associated with the assignment.
     * The resulting DTO is used to transfer assignment and submission details to the client.
     *
     * @param assignmentDetailsDTO The `AssignmentDetailsDTO` containing the details of the assignment.
     * @param submissionResponseDTOList A list of `SubmissionResponseDTO` objects representing the submissions for the assignment.
     * @return An `AssignmentResponseDTO` containing the assignment details and a list of submission details.
     */
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

    /**
     * Converts message data into a MessageResponseDTO for data transfer.
     *
     * This method creates a `MessageResponseDTO` by extracting details from a given `Message` object. It populates the DTO
     * with the message's ID, content, read status, and timestamp. The resulting DTO is used to transfer message details to the client.
     *
     * @param messageDetails The `Message` object containing the details of the message to be converted.
     * @return A `MessageResponseDTO` containing the message details, including its ID, content, read status, and sent time.
     */
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

    /**
     * Creates a CourseMessagesResponseDTO by transforming a list of messages and user details.
     *
     * This method creates a `CourseMessagesResponseDTO` that encapsulates user details and a list of message data. It iterates
     * through the provided list of `Message` objects and converts each into a `MessageResponseDTO` using the `createMessageResponseDTO` method.
     * The final DTO contains the user details and the list of message DTOs, which is used to transfer course-related message data.
     *
     * @param userDetails A map containing details about the user, typically their ID or role, to be included in the response.
     * @param messages A list of `Message` objects to be converted into `MessageResponseDTO` objects.
     * @return A `CourseMessagesResponseDTO` containing the user details and the list of message response DTOs.
     */
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

    /**
     * Creates an AssignmentDetailsDTO from the provided Assignment object.
     *
     * This method transforms an `Assignment` object into an `AssignmentDetailsDTO`, which contains relevant assignment details
     * such as the title, description, deadline, and ID. This DTO can be used to return assignment-specific information in a structured format.
     *
     * @param assignmentDetails The `Assignment` object from which the details are extracted.
     * @return An `AssignmentDetailsDTO` containing the assignment's title, description, deadline, and ID.
     */
    @Override
    public AssignmentDetailsDTO createAssignmentDetailsDTO(Assignment assignmentDetails)
    {
        //Create the assignmentDetails DTO Object for the Assignment Response DTO
        AssignmentDetailsDTO assignmentDetailsDTO = new AssignmentDetailsDTO();
        //Setting the details from the assignment object
        assignmentDetailsDTO.setTitle(assignmentDetails.getTitle() );
        assignmentDetailsDTO.setDescription(assignmentDetails.getDescription());
        assignmentDetailsDTO.setDeadline(  assignmentDetails.getDeadline() );
        assignmentDetailsDTO.setId( assignmentDetails.getId() );

        //Return the DTO object
        return assignmentDetailsDTO;
    }
}
