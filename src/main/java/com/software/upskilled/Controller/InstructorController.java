package com.software.upskilled.Controller;

import com.software.upskilled.Entity.*;
import com.software.upskilled.dto.*;
import com.software.upskilled.service.*;
import com.software.upskilled.utils.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/instructor")
public class InstructorController {

    private final FileService fileService;

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private final InstructorCourseAuth instructorCourseAuth;

    @Autowired
    private final CourseMaterialService courseMaterialService;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private GradeBookService gradeBookService;

    @Autowired
    private final CoursePropertyValidator coursePropertyValidator;

    @Autowired
    private final AssignmentPropertyValidator assignmentPropertyValidator;

    @Autowired
    private final ErrorResponseMessageUtil errorResponseMessageUtil;

    @Autowired
    private final SucessResponseMessageUtil sucessResponseMessageUtil;

    @Autowired
    private final CreateDTOObjectsImpl dtoObjectsCreator;
    @Autowired
    private MessageService messageService;


    @GetMapping("/hello")
    public String hello(){
        return "Hello Instructor";
    }

    /**
     * Endpoint to retrieve details of the currently authenticated user.
     *
     * @param user the authenticated user object, injected by Spring Security.
     * @return a CreateUserDTO object containing sanitized user information such as
     *         ID, email, role, first name, last name, designation, and status.
     *         The password field is obfuscated for security.
     */
    @GetMapping("/me")
    public CreateUserDTO getCurrentUser(@AuthenticationPrincipal Users user) {
        CreateUserDTO userDTO = new CreateUserDTO();
        userDTO.setId(user.getId());
        userDTO.setEmail(user.getEmail());
        userDTO.setRole(user.getRole());
        userDTO.setPassword("*******");
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setDesignation(user.getDesignation());
        userDTO.setStatus(user.getStatus());
        return userDTO;
    }

    /**
     * Endpoint to retrieve a list of courses assigned to the authenticated instructor.
     *
     * @param authentication the authentication object containing the user's details,
     *                       injected by Spring Security.
     * @return a ResponseEntity containing a list of CourseInfoDTO objects. Each DTO
     *         includes course details such as ID, title, description, instructor details,
     *         name, and status. Only active courses are included in the response, sorted
     *         by their last updated timestamp in descending order.
     */
    @GetMapping("/courses")
    public ResponseEntity<List<CourseInfoDTO>> viewCoursesForInstructor(Authentication authentication) {

        String email = authentication.getName();
        Users instructor = userService.findUserByEmail(email);

        List<CourseInfoDTO> courseList =  courseService.findByInstructorId(instructor.getId())
                .stream()
                .filter(course -> course.getStatus().equals(Course.Status.ACTIVE))
                .sorted(Comparator.comparing(Course::getUpdatedAt).reversed())
                .map((course -> {
            CourseInfoDTO courseInfoDTO = new CourseInfoDTO();
            courseInfoDTO.setId(course.getId());
            courseInfoDTO.setTitle(course.getTitle());
            courseInfoDTO.setDescription(course.getDescription());
            courseInfoDTO.setInstructorId(course.getInstructor().getId());
            courseInfoDTO.setInstructorName(course.getInstructor().getFirstName() + " " + course.getInstructor().getLastName());
            courseInfoDTO.setName(course.getName());
            courseInfoDTO.setStatus(course.getStatus());
            return courseInfoDTO;
        })).collect(Collectors.toList());
        return ResponseEntity.ok(courseList);
    }

    /**
     * Endpoint to retrieve detailed information about a specific course.
     *
     * @param courseId       the ID of the course to retrieve.
     * @param authentication the authentication object containing the user's details,
     *                       injected by Spring Security.
     * @return a ResponseEntity containing a CourseInfoDTO object with detailed course
     *         information such as ID, title, description, instructor details, name, and status.
     *         If the authenticated user is not authorized to access the course, an appropriate
     *         error response is returned.
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getCourseDetails(@PathVariable Long courseId, Authentication authentication) {

        Course course = courseService.findCourseById(courseId);

        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseId,authentication);

        if (authResponse != null) {
            return authResponse;
        }

        CourseInfoDTO courseInfoDTO = new CourseInfoDTO();
        courseInfoDTO.setId(course.getId());
        courseInfoDTO.setTitle(course.getTitle());
        courseInfoDTO.setDescription(course.getDescription());
        courseInfoDTO.setInstructorId(course.getInstructor().getId());
        courseInfoDTO.setInstructorName(course.getInstructor().getFirstName() + " " + course.getInstructor().getLastName());
        courseInfoDTO.setName(course.getName());
        courseInfoDTO.setStatus(course.getStatus());

        return ResponseEntity.ok(courseInfoDTO);
    }

    /**
     * Endpoint to retrieve a list of announcements for a specific course, intended for editing purposes.
     *
     * @param courseId       the ID of the course whose announcements are to be retrieved.
     * @param authentication the authentication object containing the user's details,
     *                       injected by Spring Security.
     * @return a ResponseEntity containing a list of AnnouncementRequestDTO objects, each
     *         representing an announcement with details such as ID, title, content, and last updated timestamp.
     *         If the authenticated user is not authorized to access the course, an appropriate error response is returned.
     */
    // View announcements for a specific course
    @GetMapping("/course/{courseId}/announcements")
    public ResponseEntity<?> viewAnnouncementsForEditing(
            @PathVariable Long courseId, Authentication authentication) {

        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseId, authentication);

        if (authResponse != null) {
            return authResponse;
        }

        // Fetch the announcements for the course
        Set<Announcement> announcements = announcementService.getAnnouncementsByCourseId(courseId);

        // Convert announcements to AnnouncementDTO
        List<AnnouncementRequestDTO> announcementDTOs = announcements.stream()
                .sorted(Comparator.comparing(Announcement::getUpdatedAt).reversed())
                .map(announcement -> new AnnouncementRequestDTO(announcement.getId(),announcement.getTitle(), announcement.getContent(),announcement.getUpdatedAt()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(announcementDTOs);
    }

    /**
     * Endpoint to retrieve a list of announcements for a specific course, intended for editing purposes.
     *
     * @param courseId       the ID of the course whose announcements are to be retrieved.
     * @param authentication the authentication object containing the user's details,
     *                       injected by Spring Security.
     * @return a ResponseEntity containing a list of AnnouncementRequestDTO objects, each
     *         representing an announcement with details such as ID, title, content, and last updated timestamp.
     *         If the authenticated user is not authorized to access the course, an appropriate error response is returned.
     */
    // Create a new announcement
    @PostMapping("/course/{courseId}/announcement")
    public ResponseEntity<String> createAnnouncement(
            @PathVariable Long courseId,
            @RequestBody AnnouncementDTO announcementDTO,
            Authentication authentication) {

        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseId, authentication);

        if (authResponse != null) {
            return authResponse;
        }

        Course course = courseService.findCourseById(courseId);

        if(announcementDTO.getTitle().isBlank() || announcementDTO.getContent().isBlank()) {
            return ResponseEntity.badRequest().body("Title or content missing!");
        }

        Announcement announcement = Announcement.builder()
                        .title(announcementDTO.getTitle())
                        .content(announcementDTO.getContent())
                                .course(course).build();

        announcementService.saveAnnouncement(announcement);

        return ResponseEntity.ok("Announcement created successfully");
    }


    /**
     * Endpoint to retrieve a specific announcement by its ID.
     *
     * @param id             the ID of the announcement to be retrieved.
     * @param authentication the authentication object containing the user's details,
     *                       injected by Spring Security.
     * @return a ResponseEntity containing the AnnouncementRequestDTO with details such as
     *         ID, title, content, and last updated timestamp.
     *         Returns a 404 error response if the announcement does not exist, or an
     *         error response if the authenticated user is not authorized to access the course.
     */
    @GetMapping("/getAnnouncementById/{id}")
    public ResponseEntity<?> getAnnouncementById(
            @PathVariable Long id, Authentication authentication) {

        Announcement announcement = announcementService.findAnnouncementById(id);

        if (announcement == null) {
            //Create Appropriate Error Message
            return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.NOT_FOUND.value(), "The announcement corresponding to this announcement ID does not exist");
        }

        Course course = announcement.getCourse();

        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(course.getId(), authentication);

        if (authResponse != null) {
            return authResponse;
        }

        AnnouncementRequestDTO announcementDTO = new AnnouncementRequestDTO();
        announcementDTO.setId(announcement.getId());
        announcementDTO.setContent(announcement.getContent());
        announcementDTO.setTitle(announcement.getTitle());
        announcementDTO.setUpdatedAt(announcement.getUpdatedAt());

        return ResponseEntity.ok(announcementDTO);

    }

    /**
     * Endpoint to edit an existing announcement.
     *
     * @param announcementId the ID of the announcement to be edited.
     * @param announcementDTO the AnnouncementDTO object containing the updated title and content.
     * @param authentication  the authentication object containing the user's details,
     *                        injected by Spring Security.
     * @return a ResponseEntity with a success message upon successful update of the announcement.
     *         Returns a 404 error response if the announcement does not exist, a bad request response
     *         if the title or content is missing, or an error response if the authenticated user is
     *         not authorized to access the course.
     */
    // Edit an existing announcement
    @PutMapping("/announcement/{announcementId}")
    public ResponseEntity<?> editAnnouncement(
            @PathVariable Long announcementId,
            @RequestBody AnnouncementDTO announcementDTO,
            Authentication authentication) {

        Announcement announcement = announcementService.findAnnouncementById(announcementId);

        if (announcement == null) {
            //Create Appropriate Error Message
            return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.NOT_FOUND.value(), "The announcement corresponding to this announcement ID does not exist");
        }

        Course course = announcement.getCourse();

        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(course.getId(), authentication);

        if (authResponse != null) {
            return authResponse;
        }

        if(announcementDTO.getTitle().isBlank() || announcementDTO.getContent().isBlank()) {
            return ResponseEntity.badRequest().body("Title or content missing!");
        }


        announcement.setTitle(announcementDTO.getTitle());
        announcement.setContent(announcementDTO.getContent());
        announcementService.saveAnnouncement(announcement);

        return ResponseEntity.ok("Announcement updated successfully");
    }

    // Delete an existing announcement
    /**
     * Endpoint to delete an existing announcement by its ID.
     *
     * @param announcementId the ID of the announcement to be deleted.
     * @param authentication the authentication object containing the user's details,
     *                       injected by Spring Security.
     * @return a ResponseEntity with a success message upon successful deletion of the announcement.
     *         Returns a bad request response if the announcement does not exist, or an error
     *         response if the authenticated user is not authorized to access the course.
     */
    @DeleteMapping("/deleteAnnouncementById/{announcementId}")
    public ResponseEntity<String> deleteAnnouncement(
            @PathVariable Long announcementId,
            Authentication authentication) {

        Announcement announcement = announcementService.findAnnouncementById(announcementId);

        if (announcement == null) {
            return ResponseEntity.badRequest().body("Announcement not found");
        }

        Course course = announcement.getCourse();

        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(course.getId(), authentication);

        if (authResponse != null) {
            return authResponse;
        }

        announcementService.deleteAnnouncement(announcement.getId());

        return ResponseEntity.ok("Announcement Deleted successfully");
    }

    /**
     * Endpoint to upload a syllabus file for a specific course.
     * Validates file type and instructor's authorization before proceeding with the upload.
     *
     * @param file           The syllabus file to be uploaded (only PDF is allowed).
     * @param courseId       The ID of the course for which the syllabus is being uploaded.
     * @param authentication The authentication object containing the current user's details.
     * @return A ResponseEntity containing the upload result or an error message with appropriate HTTP status.
     */
    @PostMapping("/uploadSyllabus/{courseId}")
    public ResponseEntity<?> uploadSyllabus(@RequestParam("file") MultipartFile file, @PathVariable Long courseId, Authentication authentication) {

        if (!Objects.equals(file.getContentType(), "application/pdf")) {
            return ResponseEntity.badRequest().body("Only PDF Files are allowed.");
        }

        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseId, authentication);

        if (authResponse != null) {
            return authResponse;
        }

        return new ResponseEntity<>(fileService.uploadSyllabus(file,courseId), HttpStatus.OK);
    }

    /**
     * Endpoint to retrieve and view the syllabus for a specific course.
     * Verifies if the syllabus is uploaded and returns the file for download.
     *
     * @param courseId The ID of the course whose syllabus is being requested.
     * @return A ResponseEntity containing the syllabus file or an error message if not found.
     */
    @GetMapping("/{courseId}/syllabus")
    public ResponseEntity<?>  viewSyllabus(@PathVariable Long courseId) {

        // Find the course by ID
        Course course = courseService.findCourseById(courseId);

        if (course == null) {
            return ResponseEntity.badRequest().body("Invalid course ID");
        }

        // Check if a syllabus is uploaded
        String syllabusUrl = course.getSyllabusUrl();

        if (syllabusUrl == null || syllabusUrl.isEmpty()) {
            return ResponseEntity.badRequest().body("No syllabus uploaded for this course.");
        }

        final byte[] data = fileService.viewSyllabus(courseId);
        final ByteArrayResource resource = new ByteArrayResource(data);


        return ResponseEntity
                .ok()
                .contentLength(data.length)
                .header("Content-disposition", "attachment; filename=\"" + syllabusUrl + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);

    }

    /* Assignment's endpoint */
    /**
     * Endpoint to create a new assignment for a specific course.
     * Verifies instructor authorization, validates assignment details, and creates the assignment.
     *
     * @param courseId   The ID of the course for which the assignment is being created.
     * @param assignment The assignment details to be created.
     * @param authentication The authentication details of the instructor creating the assignment.
     * @return A ResponseEntity with the result of the assignment creation or an error message.
     */
    @PostMapping("/{courseId}/assignment/create")
    public ResponseEntity<String> createAssignment(@PathVariable Long courseId,
                                                       @RequestBody Assignment assignment,Authentication authentication) {

        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseId, authentication);

        if (authResponse != null) {
            return authResponse;
        }

        String email = authentication.getName();

        Users instructor = userService.findUserByEmail(email);

        Course course = courseService.findCourseById(courseId);

        if((assignment.getTitle().isBlank() || assignment.getDescription().isBlank()) || assignment.getDeadline() == null) {
            return ResponseEntity.badRequest().body("Title or content missing!");
        }

        long currentEpoch = System.currentTimeMillis();

        if (currentEpoch > assignment.getDeadline()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("The deadline must be a future date.");
        }

        // Set course and creator (instructor)
        assignment.setCourse(course);
        assignment.setCreatedBy(instructor);

        assignmentService.createAssignment(assignment);

        return ResponseEntity.ok("Assignment Created successfully.");
    }

    /**
     * Endpoint to retrieve assignment details by its ID.
     * Verifies instructor authorization and returns assignment details in a response DTO.
     *
     * @param assignmentId The ID of the assignment to retrieve.
     * @param authentication The authentication details of the instructor requesting the assignment.
     * @return A ResponseEntity containing the assignment details or an error message if not found.
     */
    @GetMapping("/getAssignmentById/{assignmentId}")
    public ResponseEntity<?> getAssignmentById(@PathVariable Long assignmentId, Authentication authentication) {

        Assignment assignment = assignmentService.getAssignmentById(assignmentId);

        if(assignment == null) return ResponseEntity.badRequest().body("Invalid Assignnment ID");

        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(assignment.getCourse().getId(), authentication);

        if (authResponse != null) {
            return authResponse;
        }

        //Create the DTO for the Assignment Details Object
        AssignmentDetailsDTO assignmentDetailsDTO = dtoObjectsCreator.createAssignmentDetailsDTO( assignment );

        //Create the Assignment Response DTO Object
        AssignmentResponseDTO assignmentResponseDTO = dtoObjectsCreator.createAssignmentResponseDTO( assignmentDetailsDTO, null );

        return ResponseEntity.ok(assignmentResponseDTO);

    }


    // Update an assignment (only for instructors)
    /**
     * Endpoint to update an existing assignment for a specific course.
     * Verifies instructor authorization, checks assignment details, and updates the assignment.
     *
     * @param courseId        The ID of the course to which the assignment belongs.
     * @param assignmentId    The ID of the assignment to be updated.
     * @param updatedAssignment The updated assignment details.
     * @param authentication  The authentication details of the instructor making the update request.
     * @return A ResponseEntity with the result of the assignment update or an error message.
     */
    @PutMapping("/{courseId}/assignment/{assignmentId}")
    public ResponseEntity<String> updateAssignment(@PathVariable Long courseId,
                                                       @PathVariable Long assignmentId,
                                                       @RequestBody Assignment updatedAssignment,
                                                   Authentication authentication) {

        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseId, authentication);

        if (authResponse != null) {
            return authResponse;
        }

        Course course = courseService.findCourseById(courseId);

        Assignment existingAssignment = assignmentService.getAssignmentById(assignmentId);

        if(existingAssignment == null) return ResponseEntity.badRequest().body("Invalid Assignnment ID");

        if(!existingAssignment.getCourse().getId().equals(course.getId())) {
            return ResponseEntity.status(403).body("This assignment doesn't belongs to the course");
        }

        if(updatedAssignment.getTitle().isBlank() || updatedAssignment.getDescription().isBlank() || updatedAssignment.getDeadline() == null) {
            return ResponseEntity.badRequest().body("Title, description or deadline missing!");
        }

        existingAssignment.setTitle(updatedAssignment.getTitle());
        existingAssignment.setDescription(updatedAssignment.getDescription());
        existingAssignment.setDeadline(updatedAssignment.getDeadline());

        assignmentService.updateAssignment(existingAssignment);
        return ResponseEntity.ok("Assignment updated successfully");
    }

    // Delete an assignment (only for instructors)
    /**
     * Endpoint to delete an existing assignment for a specific course.
     * Verifies instructor authorization, checks assignment validity, and deletes the assignment.
     *
     * @param courseId       The ID of the course from which the assignment is being deleted.
     * @param assignmentId   The ID of the assignment to be deleted.
     * @param authentication The authentication details of the instructor requesting the deletion.
     * @return A ResponseEntity with the result of the assignment deletion or an error message.
     */
    @DeleteMapping("/{courseId}/assignment/{assignmentId}")
    public ResponseEntity<String> deleteAssignment(@PathVariable Long courseId,
                                                 @PathVariable Long assignmentId,
                                                 Authentication authentication) {

        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseId, authentication);

        if (authResponse != null) {
            return authResponse;
        }

        Course course = courseService.findCourseById(courseId);

        Assignment existingAssignment = assignmentService.getAssignmentById(assignmentId);

        if(existingAssignment == null) return ResponseEntity.badRequest().body("Invalid Assignnment ID");

        // Check if the assignment is assigned to this course
        if(!existingAssignment.getCourse().getId().equals(course.getId())) {
            return ResponseEntity.status(403).body("This assignment doesn't belongs to the course");
        }

        // delete the assignment
        assignmentService.deleteAssignment(assignmentId);

        return ResponseEntity.ok("Assignment Deleted successfully");
    }

    /**
     * Endpoint to retrieve all assignments for a specific course.
     * Verifies instructor authorization and returns a list of assignments for the course.
     *
     * @param courseId       The ID of the course whose assignments are being requested.
     * @param authentication The authentication details of the instructor making the request.
     * @return A ResponseEntity containing the list of assignments for the course or an error message.
     */
    @GetMapping("/course/{courseId}/assignments")
    public ResponseEntity<?> getAssignmentsForTheCourse(@PathVariable Long courseId, Authentication authentication) {
        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseId, authentication);

        if (authResponse != null) {
            return authResponse;
        }


        List<AssignmentResponseDTO> assignmentsList = assignmentService.getAllAssignmentsSortedByDeadLine(courseId).stream()
                .map(assignment -> {

                    //Create the DTO for the Assignment Details Object
                    AssignmentDetailsDTO assignmentDetailsDTO = dtoObjectsCreator.createAssignmentDetailsDTO( assignment );

                    //Create the assignment response dto by calling the DTO object
                    return dtoObjectsCreator.createAssignmentResponseDTO( assignmentDetailsDTO, null );
                }).toList();

        return ResponseEntity.ok(assignmentsList);
    }

    /**
     * Endpoint to retrieve all submissions for a specific assignment in a course.
     * Verifies instructor authorization and checks assignment ownership before returning submission details.
     *
     * @param courseID        The ID of the course containing the assignment.
     * @param assignmentId    The ID of the assignment for which submissions are requested.
     * @param authentication  The authentication details of the instructor requesting the submissions.
     * @return A ResponseEntity containing the assignment submission details or an error message.
     */
    @GetMapping("/{courseID}/{assignmentId}/submissions")
    public ResponseEntity<?> getAssignmentSubmissions(@PathVariable Long courseID, @PathVariable Long assignmentId, Authentication authentication)
    {
        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseID, authentication);

        if (authResponse != null) {
            return authResponse;
        }

        //Check if the assignment actually belongs to the course
        Map<String,Long> propertyKeyValue = new HashMap<>();
        propertyKeyValue.put("assignment", assignmentId);
        //Pass the courseID and the associated property to the validator
        //If not, then we send the error message that the assignment is not the property of the course
        if( !coursePropertyValidator.isPropertyOfTheCourse( courseID, propertyKeyValue ) )
        {
            return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.BAD_REQUEST.value(), "This assignment doesn't belong to the particular course");
        }

        //Get the Assignment details by passing the assignmentID
        Assignment assignmentDetails = assignmentService.getAssignmentById( assignmentId );
        //Check if the assignmentDetails is null
        if( assignmentDetails == null )
        {
            return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.NOT_FOUND.value(), "This assignment doesn't exist" );
        }
        else
        {
            //Obtain the set of the submissions and send the details
            List<Submission> assignmentSubmissions = submissionService.getSubmissionsSortedBySubmittedTime( assignmentDetails.getId() );
            if( assignmentSubmissions.isEmpty() ) {

                AssignmentDetailsDTO assignmentDetailsDTO = dtoObjectsCreator.createAssignmentDetailsDTO( assignmentDetails );

                //Create the AssignmentResponse DTO by sending the details
                AssignmentResponseDTO assignmentResponseDTO = dtoObjectsCreator.createAssignmentResponseDTO(assignmentDetailsDTO, null);

                return ResponseEntity.ok(assignmentResponseDTO);
            }
            else
            {
                List<SubmissionResponseDTO> submissionResponseDTOList = new ArrayList<>();
                assignmentSubmissions.forEach( assignmentSubmission -> {

                    //Get the user details for each submission
                    Users submissionEmployeeDetails = assignmentSubmission.getEmployee();
                    //Get the Submission DTO response object by invoking the DTO Object creator
                    SubmissionResponseDTO submissionResponseDTO = dtoObjectsCreator.createSubmissionDTO( assignmentSubmission, assignmentDetails, submissionEmployeeDetails );
                    //Adding the submission Response DTO to the list
                    submissionResponseDTOList.add( submissionResponseDTO );
                });

                //Create the DTO for the Assignment Details Object
                AssignmentDetailsDTO assignmentDetailsDTO = dtoObjectsCreator.createAssignmentDetailsDTO( assignmentDetails );

                //Create the AssignmentResponse DTO by sending the details
                AssignmentResponseDTO assignmentResponseDTO = dtoObjectsCreator.createAssignmentResponseDTO( assignmentDetailsDTO, submissionResponseDTOList );

                return ResponseEntity.ok( assignmentResponseDTO );
            }

        }
    }

    /**
     * Endpoint to retrieve and view a particular assignment submission.
     * Verifies instructor authorization and returns the submission file for download.
     *
     * @param assignmentId    The ID of the assignment to which the submission belongs.
     * @param courseID        The ID of the course containing the assignment.
     * @param submissionID    The ID of the particular submission to be viewed.
     * @param authentication  The authentication details of the instructor requesting the submission.
     * @return A ResponseEntity containing the assignment submission file or an error message if not found.
     */
    @GetMapping("/{courseID}/assignments/{assignmentId}/submissions/{submissionID}/viewSubmission")
    public ResponseEntity<?> viewParticularAssignmentSubmission(@PathVariable Long assignmentId, @PathVariable Long courseID, @PathVariable Long submissionID, Authentication authentication){

        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseID, authentication);

        if (authResponse != null) {
            return authResponse;
        }

        //Get the submission details associated with the ID
        Submission uploadedSubmissionDetails = submissionService.getSubmissionByID( submissionID );
        if( uploadedSubmissionDetails == null )
            return ResponseEntity.badRequest().body("Submission not found");
        else
        {
            final byte[] data = fileService.viewAssignmentSubmission( uploadedSubmissionDetails.getSubmissionUrl() );
            final ByteArrayResource resource = new ByteArrayResource(data);


            return ResponseEntity
                    .ok()
                    .contentLength(data.length)
                    .header("Content-disposition", "attachment; filename=\"" + uploadedSubmissionDetails.getSubmissionUrl() + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        }
    }

    /**
     * Endpoint to retrieve details of a particular submission for a specific assignment.
     * Verifies instructor authorization, checks assignment-submission validity, and returns submission details.
     *
     * @param assignmentId    The ID of the assignment for which the submission details are requested.
     * @param courseID        The ID of the course containing the assignment.
     * @param submissionID    The ID of the specific submission to retrieve details for.
     * @param authentication  The authentication details of the instructor requesting the submission details.
     * @return A ResponseEntity containing the submission details or an error message if not found.
     */
    @GetMapping("/{courseID}/assignments/{assignmentId}/submissions/{submissionID}")
    public ResponseEntity<?> getSubmissionDetailsForParticularSubmission( @PathVariable Long assignmentId, @PathVariable Long courseID, @PathVariable Long submissionID, Authentication authentication ) throws IOException {

        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseID, authentication);

        if (authResponse != null) {
            return authResponse;
        }
        //Check if the assignment actually belongs to the course
        Map<String,Long> propertyKeyValue = new HashMap<>();
        propertyKeyValue.put("assignment", assignmentId);
        //Pass the courseID and the associated property to the validator
        //If not, then we send the error message that the assignment is not the property of the course
        if( !coursePropertyValidator.isPropertyOfTheCourse( courseID, propertyKeyValue ) )
        {
            return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.BAD_REQUEST.value(), "This assignment doesn't belong to this course");
        }
        //Get the assignment Details from the Database and check if the submission ID exist for the property
        Assignment assignmentDetails = assignmentService.getAssignmentById( assignmentId );
        if ( assignmentDetails == null )
        {
            return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.NOT_FOUND.value(), "Assignment not found");
        }
        //Check if the submissionID is part of the assignment, If not then send appropriate error message
        if ( !assignmentPropertyValidator.validateSubmissionAgainstAssignment( assignmentId, submissionID ) )
        {
            return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.BAD_REQUEST.value(), "Particular Submission ID does not correspond to this assignment");
        }
        //Fetch the Submission Details for the Particular ID
        Submission submission = submissionService.getSubmissionByID( submissionID );
        //Check if the submission exists
        if( submission == null )
        {
           return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.BAD_REQUEST.value(), "Particular Submission does not exist for this assignment");
        }
        else
        {
            //Get the employee details of the submitted assignment
            Users employeeDetails = submission.getEmployee();
            //Get the SubmissionResponse DTO Object
            SubmissionResponseDTO submissionResponseDTO = dtoObjectsCreator.createSubmissionDTO( submission, assignmentDetails, employeeDetails );
            return ResponseEntity.ok( submissionResponseDTO );
        }

    }

    /**
     * Endpoint to submit grades for a specific assignment submission to the gradebook.
     * Verifies instructor authorization, validates grading details, and updates the gradebook with the grade and feedback.
     *
     * @param submissionID    The ID of the submission being graded.
     * @param courseID        The ID of the course for which the assignment belongs.
     * @param authentication  The authentication details of the instructor submitting the grades.
     * @param gradingDetails  The grading details including the grade and feedback.
     * @return A ResponseEntity containing the gradebook response or an error message if validation fails.
     */
    @PostMapping("/gradeBook/gradeAssignment")
    public ResponseEntity<?> submitGradesToGradeBook(@RequestParam("submissionId") String submissionID, @RequestParam("courseId") String courseID, Authentication authentication, @RequestBody GradeBookRequestDTO gradingDetails)
    {
        //Obtaining the email of the user from the authentication object
        String email = authentication.getName();
        //Obtaining the instructor details
        Users instructor = userService.findUserByEmail(email);
        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(Long.parseLong( courseID ), authentication);

        if (authResponse != null) {
            return authResponse;
        }

        //Get the submission details associated with the ID
        Submission uploadedSubmissionDetails = submissionService.getSubmissionByID( Long.parseLong( submissionID ));
        if( uploadedSubmissionDetails == null )
            return ResponseEntity.badRequest().body("Submission not found");
        else
        {
            Gradebook gradeBookDetails = uploadedSubmissionDetails.getGrade();
            if( gradeBookDetails != null )
                return ResponseEntity.badRequest().body("Grades already exist for this particular submission");
            //Check for the valid grade details
            else if( gradingDetails == null )
                return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.BAD_REQUEST.value(), "Please pass the grading Details");
            else if( gradingDetails.getGrade() == null )
                return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.BAD_REQUEST.value(), "The instructor must pass the grade value" );
            else if( gradingDetails.getGrade() < 0 || gradingDetails.getGrade() > 100 )
                return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.BAD_REQUEST.value(), "The grade must be between 0 and 100");
            else{

                //Construct the new GradeBook object so that it can the submission can be saved
                Gradebook gradeBookSubmission = Gradebook.builder().
                        grade( gradingDetails.getGrade() ).
                        feedback(gradingDetails.getFeedback() == null ? "" : gradingDetails.getFeedback() )
                        .submission( uploadedSubmissionDetails )
                        .instructor(instructor).build();

                //Provide the GradeBook submission to the GradeBook service so that it can be stored in the database
                Gradebook submittedGradeBookSubmission = gradeBookService.saveGradeBookSubmission( gradeBookSubmission );

                //Also, we have to update the submission Status to Graded so that it is reflected.
                //Modifying the Grading status of the uploaded submission
                uploadedSubmissionDetails.setStatus( Submission.Status.GRADED );
                //Saving the submission details in the database
                Submission modifiedUploadedSubmissionDetails = submissionService.modifySubmissionDetails( uploadedSubmissionDetails );

                //Create GradeBook response DTO object
                GradeBookResponseDTO gradeBookResponseDTO = new GradeBookResponseDTO();
                gradeBookResponseDTO.setGrade( submittedGradeBookSubmission.getGrade() );
                gradeBookResponseDTO.setFeedback( submittedGradeBookSubmission.getFeedback() );
                gradeBookResponseDTO.setGradedDate( submittedGradeBookSubmission.getGradedAt() );
                gradeBookResponseDTO.setSubmissionID( modifiedUploadedSubmissionDetails.getId() );
                gradeBookResponseDTO.setInstructorID( instructor.getId() );


                //Create
                return ResponseEntity.ok( gradeBookResponseDTO );
            }
        }
    }

    /**
     * Endpoint to update the grade and feedback for a specific assignment submission in the gradebook.
     * Verifies instructor authorization and validates the new grading details before updating the gradebook.
     *
     * @param gradeBookRequestDTO The new grading details including grade and feedback.
     * @param gradingID           The ID of the grading record to be updated.
     * @param authentication      The authentication details of the instructor updating the grades.
     * @return A ResponseEntity containing the updated gradebook details or an error message if validation fails.
     */
    @PutMapping("/gradeBook/updateGradeAssignment")
    public ResponseEntity<?> updateGradeDetails( @RequestBody GradeBookRequestDTO gradeBookRequestDTO,
                                                 @RequestParam("gradingId") long gradingID,
                                                 Authentication authentication)
    {
        //Getting the courseID details from the grading ID
        Gradebook gradeBookDetails = gradeBookService.getGradeBookByID( gradingID );
        //Check if the gradeBook Details is null
        if( gradeBookDetails == null )
            return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.BAD_REQUEST.value(), "This gradingID doesn't correspond to any grading ID");

        //Getting the submission and from there getting the course ID
        long courseID = gradeBookDetails.getSubmission().getAssignment().getCourse().getId();
        //Validating the instructor
        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseID, authentication);
        if (authResponse != null) {
            return authResponse;
        }

        //If grade ius present and the new grade score is between 0 and 100 then set it to set to new grade ,else don't
        if( (gradeBookRequestDTO.getGrade() != null) && ( gradeBookRequestDTO.getGrade() >= 0 && gradeBookRequestDTO.getGrade() <= 100)  ) {
                gradeBookDetails.setGrade(gradeBookRequestDTO.getGrade());
        }
        //Check if the feedback is present in the DTO object and it is not blank, then only update
        if( (gradeBookRequestDTO.getFeedback() != null) && (!gradeBookRequestDTO.getFeedback().isBlank()))
            gradeBookDetails.setFeedback(gradeBookRequestDTO.getFeedback() );

        //Saving the updated value into the database
        gradeBookService.saveGradeBookSubmission( gradeBookDetails );

        //Get the GradeBook Response DTO Object from the dto object creator
        GradeBookResponseDTO gradeBookResponseDTO = dtoObjectsCreator.createGradeBookResponseDTO( gradeBookDetails,
                gradeBookDetails.getInstructor().getId(), gradeBookDetails.getSubmission().getId() );


        //Saving the new details into the database and sending the response object back
        return new ResponseEntity<>( gradeBookResponseDTO, HttpStatus.OK );

    }

    /**
     * Endpoint to upload course material for a specific course.
     * Verifies instructor authorization, validates file type, and uploads the course material.
     *
     * @param file                  The course material file to be uploaded (PDF only).
     * @param courseId              The ID of the course for which the material is being uploaded.
     * @param courseMaterialTitle   The title of the course material.
     * @param courseMaterialDescription The description of the course material.
     * @param authentication        The authentication details of the instructor uploading the material.
     * @return A ResponseEntity containing the result of the upload or an error message if validation fails.
     */
    @PostMapping("/uploadCourseMaterial/{courseId}")
    public ResponseEntity<?> uploadCourseMaterial(@RequestParam("file") MultipartFile file, @PathVariable Long courseId,
                                                  @RequestParam("materialTitle") String courseMaterialTitle,
                                                  @RequestParam("materialDescription") String courseMaterialDescription,
                                                  Authentication authentication) {

        if (!Objects.equals(file.getContentType(), "application/pdf")) {
            return ResponseEntity.badRequest().body("Only PDF Files are allowed.");
        }

        String email = authentication.getName();

        Users instructor = userService.findUserByEmail(email);

        Course course = courseService.findCourseById(courseId);

        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseId, authentication);

        if (authResponse != null) {
            return authResponse;
        }

        String instructorName = instructor.getFirstName()+"_"+instructor.getLastName()+"_"+instructor.getId();
        String courseTitle = course.getTitle()+"_"+course.getId();

        CourseMaterialDTO courseMaterialDetails= CourseMaterialDTO.builder()
                .materialTitle( courseMaterialTitle )
                .materialDescription( courseMaterialDescription )
                .build();

        return new ResponseEntity<>(fileService.uploadCourseMaterial( file, instructorName, courseTitle, courseMaterialDetails), HttpStatus.OK);
    }

    /**
     * Endpoint to retrieve all course materials for a specific course.
     * Verifies instructor authorization and returns a list of course materials for the course.
     *
     * @param courseId       The ID of the course for which materials are being requested.
     * @param authentication The authentication details of the instructor making the request.
     * @return A ResponseEntity containing a list of course materials or an empty list if none are available.
     */
    @GetMapping("/getCourseMaterials/{courseId}")
    public ResponseEntity<?> getAllCourseMaterials(@PathVariable Long courseId, Authentication authentication)
    {
        String email = authentication.getName();

        Course course = courseService.findCourseById(courseId);

        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseId, authentication);

        if (authResponse != null) {
            return authResponse;
        }

        Set<CourseMaterial> courseMaterials = course.getCourseMaterials();

        if( courseMaterials.isEmpty() )
            return ResponseEntity.ok( new ArrayList<>() );
        else
        {
            List<CourseMaterialDTO> courseMaterialDTOList = courseMaterials.stream()
                            .sorted(Comparator.comparing(CourseMaterial::getUpdatedAt))
                                    .map(courseMaterial-> CourseMaterialDTO.builder()
                                                    .id(courseMaterial.getId())
                                                    .materialTitle( courseMaterial.getTitle() )
                                                    .materialDescription(courseMaterial.getDescription() ).build()).collect(Collectors.toList());

            return ResponseEntity.ok(courseMaterialDTOList);
        }
    }

    /**
     * Endpoint to retrieve and view a specific course material by its ID.
     * Verifies instructor authorization, checks course-material validity, and returns the material for download.
     *
     * @param courseId           The ID of the course containing the course material.
     * @param courseMaterialId   The ID of the specific course material to retrieve.
     * @param authentication     The authentication details of the instructor requesting the course material.
     * @return A ResponseEntity containing the course material file or an error message if not found.
     */
    @GetMapping("/getCourseMaterial/{courseId}/{courseMaterialId}")
    public ResponseEntity<?> viewCourseMaterialById(@PathVariable Long courseId, @PathVariable("courseMaterialId") Long courseMaterialId , Authentication authentication)
    {
        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseId, authentication);

        if (authResponse != null) {
            return authResponse;
        }

        //Check if the courseMaterialID belongs to this particular course
        Map<String,Long> propertyKeyValue = new HashMap<>();
        propertyKeyValue.put("courseMaterial", courseMaterialId);
        //If the courseMaterialID doesn't belong to the course, then send appropriate error message
        if( !coursePropertyValidator.isPropertyOfTheCourse( courseId, propertyKeyValue ) )
            return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.BAD_REQUEST.value(), "This courseMaterial is not part of the course");

        //Fetch the courseMaterial details
        CourseMaterial courseMaterial = courseMaterialService.getCourseMaterialById( courseMaterialId );
        //check if the CourseMaterial is null
        if( courseMaterial == null )
            return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.BAD_REQUEST.value(), "This courseMaterial doesn't exist" );

        //Fetch the CourseMaterial
        final byte[] data = fileService.viewCourseMaterial( courseMaterial.getCourseMaterialUrl() );
        final ByteArrayResource resource = new ByteArrayResource(data);

        //Get the Submission File Name
        String courseMaterialName = courseMaterial.getCourseMaterialUrl().split("/")[2];
        return ResponseEntity
                .ok()
                .contentLength(data.length)
                .header("Content-disposition", "attachment; filename=\"" + courseMaterialName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);

    }

    /**
     * Endpoint to update a specific course material for a course.
     * Verifies instructor authorization, handles file deletion, and updates the course material details (title, description, file).
     *
     * @param file                  The new course material file to be uploaded.
     * @param courseId              The ID of the course to which the material belongs.
     * @param courseMaterialId      The ID of the course material to be updated.
     * @param courseMaterialTitle   The new title for the course material (optional).
     * @param courseMaterialDescription The new description for the course material (optional).
     * @param authentication        The authentication details of the instructor updating the material.
     * @return A ResponseEntity containing the result of the update or an error message if the operation fails.
     */
    @PutMapping("/updateCourseMaterial/{courseId}/{courseMaterialId}")
    public ResponseEntity<?> updateCourseMaterial(@RequestParam(value = "file", required = true) MultipartFile file, @PathVariable Long courseId,
                                                  @PathVariable Long courseMaterialId,
                                                  @RequestParam("newMaterialTitle") Optional<String> courseMaterialTitle,
                                                  @RequestParam("newMaterialDescription") Optional<String> courseMaterialDescription,
                                                  Authentication authentication)
    {
        String email = authentication.getName();
        Users instructor = userService.findUserByEmail(email);

        Course course = courseService.findCourseById(courseId);

        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseId, authentication);

        if (authResponse != null) {
            return authResponse;
        }


        //Fetch the corresponding course material details
        CourseMaterial existingCourseMaterial = courseMaterialService.getCourseMaterialById( courseMaterialId );
        //Check if the existing Course Material is null
        if( existingCourseMaterial == null )
            return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.BAD_REQUEST.value(), "The particular course material does not exist" );

        String instructorData = instructor.getFirstName()+"_"+instructor.getLastName()+"_"+instructor.getId();
        String courseData = course.getTitle()+"_"+course.getId();

        //Try deleting the existing file first before removing the file
        boolean isExistingCourseMaterialDeleted = fileService.deleteCourseMaterial( existingCourseMaterial.getCourseMaterialUrl() ).isDeletionSuccessfull();

        //If the existing course material has been deleted then we proceed to upload the new material.
        if( isExistingCourseMaterialDeleted )
        {
            CourseMaterialDTO courseMaterialDTO = new CourseMaterialDTO();

            //Handle the Course Material Title update operation
            if( courseMaterialTitle.isPresent() )
                courseMaterialDTO.setMaterialTitle( courseMaterialTitle.get() );
            else
                courseMaterialDTO.setMaterialTitle( existingCourseMaterial.getTitle() );

            //Handle the course material description update operation
            if( courseMaterialDescription.isPresent() )
                courseMaterialDTO.setMaterialDescription( courseMaterialDescription.get() );
            else
                courseMaterialDTO.setMaterialDescription( existingCourseMaterial.getDescription() );

            //Try to update the courseMaterial
            fileService.updateCourseMaterial( file, instructorData, courseData, courseMaterialDTO, existingCourseMaterial );
            //Check if the courseMaterial has been updated
//            CourseMaterial updatedCourseMaterial = courseMaterialService.getCourseMaterialByTitle( courseMaterialDTO.getMaterialTitle() );
            CourseMaterial updatedCourseMaterial = courseMaterialService.getCourseMaterialById ( existingCourseMaterial.getId());

            if( (updatedCourseMaterial.getCourseMaterialUrl().equals( existingCourseMaterial.getCourseMaterialUrl())) ||
                    (updatedCourseMaterial.getTitle().equals( existingCourseMaterial.getTitle() ))
            || (updatedCourseMaterial.getDescription().equals( existingCourseMaterial.getDescription() ) ))
            {
                return sucessResponseMessageUtil.createSuccessResponseMessages(HttpStatus.OK.value(), "The course material details have been successfully updated");
            }
            else
                return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.INTERNAL_SERVER_ERROR.value(), "The course material details have not updated, Please contact admin ");

        }
        else
        {
            return errorResponseMessageUtil.createErrorResponseMessages(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Failed to delete the existing course material, Please try again later or contact Admin" );
        }
    }

    /**
     * Endpoint to delete a specific course material for a course.
     * Verifies instructor authorization, checks material validity, deletes the material file and database entry.
     *
     * @param courseId           The ID of the course from which the material is being deleted.
     * @param courseMaterialId   The ID of the course material to be deleted.
     * @param authentication     The authentication details of the instructor requesting the deletion.
     * @return A ResponseEntity containing the result of the deletion or an error message if the operation fails.
     */
    @DeleteMapping("/deleteCourseMaterial/{courseId}/{courseMaterialId}")
    public ResponseEntity<?> deleteCourseMaterial(@PathVariable Long courseId, @PathVariable("courseMaterialId") Long courseMaterialId, Authentication authentication)
    {
        String email = authentication.getName();

        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseId, authentication);

        if (authResponse != null) {
            return authResponse;
        }

        //Check if the course exist
        Course courseDetails = courseService.findCourseById(courseId);
        //If the course is null, send error message to the user
        if( courseDetails == null )
            return errorResponseMessageUtil.createErrorResponseMessages(HttpStatus.BAD_REQUEST.value(), "No course exist corresponding to the particular courseId");

        //Since the course exist, check if the courseMaterialId is a valid property of the course
        Map<String, Long> propertyMap = new HashMap<>();
        propertyMap.put("courseMaterial",courseMaterialId);
        //Check if the property is a valid property, if not then send appropriate error message
        if( !coursePropertyValidator.isPropertyOfTheCourse( courseId, propertyMap ) )
            return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.BAD_REQUEST.value(), "No course Material with the following ID correspond to this course");

        //Fetch the corresponding course material details
        CourseMaterial courseMaterial = courseMaterialService.getCourseMaterialById( courseMaterialId );
        //Get the course Material URL
        String courseMaterialURL = courseMaterial.getCourseMaterialUrl();

        //Try deleting the existing file first before removing the file
        boolean isExistingCourseMaterialDeleted = fileService.deleteCourseMaterial( courseMaterialURL ).isDeletionSuccessfull();

        if(  isExistingCourseMaterialDeleted )
        {
            //Delete the courseMaterial entry from the database
            courseMaterialService.deleteCourseMaterial( courseMaterialId );
            //Validate if the courseMaterial is deleted or not
            CourseMaterial deletedCourseMaterial = courseMaterialService.getCourseMaterialById( courseMaterialId );
            if( deletedCourseMaterial != null )
                return sucessResponseMessageUtil.createSuccessResponseMessages( HttpStatus.OK.value(), "Course Material has been successfully deleted" );
            else
                return sucessResponseMessageUtil.createSuccessResponseMessages( HttpStatus.OK.value(), "Failure in removing courseMaterial from database, Please contact admin" );
        }
        else
        {
            return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failure in removing the course material from storage, Please contact admin");

        }
    }

    /**
     * Endpoint to send a message to employees of a specific course.
     * Verifies instructor authorization, checks for valid course and recipient employee IDs, and sends the message to the employees.
     *
     * @param messageRequestDTO The message request containing the course ID, employee IDs, and message content.
     * @param authentication    The authentication details of the instructor sending the message.
     * @return A ResponseEntity containing a list of message response details or an error message if the operation fails.
     */
    @PostMapping("/message/sendMessage")
    public ResponseEntity<?> sendMessageToEmployees( @RequestBody MessageRequestDTO messageRequestDTO, Authentication authentication )
    {
        //Check if the courseId is present in the messageRequestDTO. If null then send error response
        if( messageRequestDTO.getCourseId() == null )
            return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.BAD_REQUEST.value(), "Course Id needs to be present in the message request");

        Long courseId = messageRequestDTO.getCourseId();

        String email = authentication.getName();

        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseId, authentication);

        if (authResponse != null) {
            return authResponse;
        }

        //Get the list of all the receiverIDs that the instructor has to send messages to
        List<Long> employeeIds = messageRequestDTO.getReceiverIds();
        //Get the course Details;
        Course courseDetails = courseService.findCourseById(courseId);
        if( employeeIds == null || employeeIds.isEmpty() )
            return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.BAD_REQUEST.value(), "Employee Ids need to be present in order for the Instructor to send messages");

        //Create a list of MessageResponseDTO  List; Skip the employee IDs which are not part of the course
        List<MessageResponseDTO> messageResponsesDTOList = employeeIds.stream().filter( employeeId -> {

            //Check if the employeeId belongs to this course
            List<Enrollment> employeeEnrollmentRecord = courseDetails.getEnrollments().stream().filter(enrollment -> {
                return Objects.equals(enrollment.getEmployee().getId(), employeeId);
            }).toList();

            //Filters the list on the basis of whether the employee belongs to the list
            return !employeeEnrollmentRecord.isEmpty();
        }).map( filteredEmployeeId ->{

            //Get the employee details
            Users employeeDetails = userService.findUserById( filteredEmployeeId );
            //Save the message object in the database
            Message instructorMessage = Message.builder().sender( courseDetails.getInstructor() )
                    .recipient( employeeDetails ).isRead( false ).course( courseDetails ).content( messageRequestDTO.getMessage() ).build();

            //Save the message in the database
            Message savedMessageDetails = messageService.createNewMessage( instructorMessage );

            //Create the Message Response DTO object by invoking the factory

            return dtoObjectsCreator.createMessageResponseDTO( savedMessageDetails );

        }).toList();

        //Send all the details of the messages to the Instructor
        return ResponseEntity.ok( messageResponsesDTOList );
    }

    /**
     * Endpoint to retrieve messages sent to employees in a specific course.
     * Verifies instructor authorization, checks for valid course and recipient employees, and returns the list of sent messages.
     *
     * @param courseId       The ID of the course for which the sent messages are being requested.
     * @param authentication The authentication details of the instructor requesting the sent messages.
     * @return A ResponseEntity containing a list of sent messages grouped by employee or an empty list if no messages are found.
     */
    @GetMapping( "/course/{courseId}/message/getSentMessages" )
    public ResponseEntity<?> getMessagesSentToEmployee( @PathVariable("courseId") Long courseId, Authentication authentication )
    {
        //Check if the employee belongs to the particular course
        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseId, authentication);
        //If authResponse is not null, then send the authResponse
        if (authResponse != null) {
            return authResponse;
        }

        //Get the courseDetails of the Particular courseId
        Course courseDetails = courseService.findCourseById(courseId);
        //Get the instructor Details from the course
        Users instructorDetails = courseDetails.getInstructor();

        //Get the unique List of employee who are the recipients of the messages from the Instructor
        List<Long> employeeIds = messageService.getUniqueListOfRecipientEmployeesForInstructor( instructorDetails.getId(), courseDetails.getId() );
        //Check if the employeeIds is empty
        if( employeeIds == null || employeeIds.isEmpty() )
            return ResponseEntity.ok( new ArrayList<>() );
        else
        {
            //We basically now create the entire array of the message object by looping over the messages, additionally, we have to group the messages by the user.
            List< CourseMessagesResponseDTO > courseMessagesResponseDTOList = employeeIds.stream().map( employeeId -> {

                //Get the employee details associated with the employeeID
                Users employeeDetails = userService.findUserById( employeeId );
                //Set the User Details in a map
                Map<String, String> userDetailsObject = new HashMap<>();
                userDetailsObject.put("name", employeeDetails.getFirstName()+ " " + employeeDetails.getLastName());
                userDetailsObject.put("email", employeeDetails.getEmail());
                userDetailsObject.put("employeeId", String.valueOf( employeeDetails.getId()));

                //Now we fetch the messages where this employee is the recipient of the message for this courseId
                Optional<List<Message>> receivedMessagesByEmployee = messageService.getAllReceivedMessageForEmployee( employeeId, courseDetails.getId() );
                //Check if the messages exist
                if( receivedMessagesByEmployee.isPresent() )
                {
                    //Get the messages received by the Employee from this Instructor for this Particular Course
                    List<Message> messages = receivedMessagesByEmployee.get();

                    //Create the course message Response DTO list
                    return dtoObjectsCreator.createCourseMessagesResponseDTO( userDetailsObject, messages );
                }
                else
                {
                    return dtoObjectsCreator.createCourseMessagesResponseDTO( userDetailsObject, new ArrayList<>() );
                }
            }).toList();
            return ResponseEntity.ok( courseMessagesResponseDTOList );
        }
    }

    /**
     * This endpoint retrieves all messages received by employees from the instructor of a specific course.
     *
     * The method first validates if the current authenticated user (instructor) is authorized to view the messages
     * for the given course. If the user is authorized, the course details are fetched, and a list of unique employee
     * IDs who have sent messages to the instructor is retrieved. For each employee, the method compiles the employee's
     * details (name, email, employee ID) and any messages they have received from the instructor for the specified course.
     *
     * The response will include a list of messages grouped by employee, or an empty list if no messages exist.
     *
     * @param courseId The ID of the course for which received messages are being fetched.
     * @param authentication The authentication information of the current user (instructor).
     * @return A ResponseEntity containing a list of course message details or an empty list if no messages are found.
     */
    @GetMapping( "/course/{courseId}/message/getReceivedMessages" )
    public ResponseEntity<?> getMessagesReceivedFromEmployee( @PathVariable("courseId") Long courseId, Authentication authentication )
    {
        //Check if the employee belongs to the particular course
        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseId, authentication);
        //If authResponse is not null, then send the authResponse
        if (authResponse != null) {
            return authResponse;
        }

        //Get the courseDetails of the Particular courseId
        Course courseDetails = courseService.findCourseById(courseId);
        //Get the instructor Details from the course
        Users instructorDetails = courseDetails.getInstructor();

        //Get the unique List of employee who have sent the mail to the Instructor
        List<Long> employeeIds = messageService.getUniqueListOfSenderEmployeesForInstructor( instructorDetails.getId(), courseDetails.getId() );
        //Check if the employeeIds is empty
        if( employeeIds == null || employeeIds.isEmpty() )
            return ResponseEntity.ok( new ArrayList<>() );
        else
        {
            //We basically now create the entire array of the message object by looping over the messages, additionally, we have to group the messages by the user.
            List< CourseMessagesResponseDTO > courseMessagesResponseDTOList = employeeIds.stream().map( employeeId -> {

                //Get the employee details associated with the employeeID
                Users employeeDetails = userService.findUserById( employeeId );
                //Set the User Details in a map
                Map<String, String> userDetailsObject = new HashMap<>();
                userDetailsObject.put("name", employeeDetails.getFirstName()+ " " + employeeDetails.getLastName());
                userDetailsObject.put("email", employeeDetails.getEmail());
                userDetailsObject.put("employeeId", String.valueOf( employeeDetails.getId()));

                //Now we fetch the messages where this employee is the recipient of the message for this courseId
                Optional<List<Message>> sentMessagesByEmployee = messageService.getAllSentMessagesForEmployee( employeeId, courseDetails.getId() );
                //Check if the messages exist
                if( sentMessagesByEmployee.isPresent() )
                {
                    //Get the messages received by the Employee from this Instructor for this Particular Course
                    List<Message> messages = sentMessagesByEmployee.get();

                    //Create the course message Response DTO list
                    return dtoObjectsCreator.createCourseMessagesResponseDTO( userDetailsObject, messages );
                }
                else
                {
                    return dtoObjectsCreator.createCourseMessagesResponseDTO( userDetailsObject, new ArrayList<>() );
                }
            }).toList();
            return ResponseEntity.ok( courseMessagesResponseDTOList );
        }
    }

    /**
     * This endpoint updates the read status of messages received by an employee from the instructor of a specific course.
     *
     * The method first validates if the current authenticated user (instructor) is authorized to update the message read
     * status for the given course. If the user is authorized, the course and instructor details are fetched, and the
     * read status of messages received by the specified employee is updated. If no rows are updated, an error response
     * is returned, otherwise, a success response is returned indicating that the read status has been updated.
     *
     * @param employeeId The ID of the employee whose message read status is being updated.
     * @param courseId The ID of the course for which the message read status is being updated.
     * @param authentication The authentication information of the current user (instructor).
     * @return A ResponseEntity indicating the success or failure of the operation.
     */
    @PutMapping("/message/readMessage")
    public ResponseEntity<?> setMessageStatusToRead( @RequestParam("employeeId") Long employeeId, @RequestParam("courseId") Long courseId, Authentication authentication )
    {
        //Check if the instructor is the valid instructor for the course
        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseId, authentication);

        if (authResponse != null) {
            return authResponse;
        }

        //Get the course details from the database
        Course courseDetails = courseService.findCourseById( courseId );
        //Get the instructor detail
        Users instructorDetails = courseDetails.getInstructor();

        int numberOfRowsUpdated = messageService.updateReadStatusOfMessagesReceivedByEmployee( instructorDetails.getId(), employeeId, courseId );

        //If the number of RowsUpdates is 0, then send error message else send ok
        if( numberOfRowsUpdated == 0 )
            return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to update teh read status of the messages");
        else
            return sucessResponseMessageUtil.createSuccessResponseMessages( HttpStatus.OK.value(), "The read status of all the messages have been updated");
    }


    /**
     * This endpoint retrieves a list of all employees (students) enrolled in a specific course.
     *
     * The method first validates if the current authenticated user (instructor) is authorized to view the enrollment
     * details for the given course. If the user is authorized, the course details are fetched, and the list of employees
     * (students) enrolled in the course is retrieved. The employee details are then mapped to UserDTO objects, which are
     * returned as part of the response. If no employees are enrolled in the course, an empty list is returned.
     *
     * @param courseId The ID of the course for which enrolled students are being retrieved.
     * @param authentication The authentication information of the current user (instructor).
     * @return A ResponseEntity containing a list of user details for all employees enrolled in the course.
     */
    @GetMapping("/course/{courseId}/getAllEmployees")
    public ResponseEntity<?> getAllEnrolledStudentsInCourse( @PathVariable("courseId") Long courseId, Authentication authentication )
    {
        //Check if the instructor is the valid instructor for the course
        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseId, authentication);

        if (authResponse != null) {
            return authResponse;
        }

        //Get the course details
        Course courseDetails = courseService.findCourseById(courseId);
        //Get all the enrollments for the particular course
        Set<Enrollment> courseEnrollmentDetails = courseDetails.getEnrollments();
        //Get the list of the User DTO
        List<CreateUserDTO> userDTOList = courseEnrollmentDetails.stream().map( enrollment -> {
           //Get the employee details from the enrollment object
           Users employeeDetails = enrollment.getEmployee();
           return dtoObjectsCreator.createUserDTO( employeeDetails );

        }).toList();
        return ResponseEntity.ok( userDTOList );
    }



}
