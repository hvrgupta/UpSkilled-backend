package com.software.upskilled.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.software.upskilled.Entity.*;
import com.software.upskilled.dto.*;
import com.software.upskilled.service.*;
import com.software.upskilled.utils.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
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
    private final CreateDTOObjectsImpl dtoObjectsCreator;


    @GetMapping("/hello")
    public String hello(){
        return "Hello Instructor";
    }

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

    @GetMapping("/courses")
    public ResponseEntity<List<CourseInfoDTO>> viewCoursesForInstructor(Authentication authentication) {

        String email = authentication.getName();
        Users instructor = userService.findUserByEmail(email);

        List<CourseInfoDTO> courseList =  courseService.findByInstructorId(instructor.getId())
                .stream()
                .filter(course -> course.getStatus().equals(Course.Status.ACTIVE))
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
                .map(announcement -> new AnnouncementRequestDTO(announcement.getId(),announcement.getTitle(), announcement.getContent(),announcement.getUpdatedAt()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(announcementDTOs);
    }

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

    @GetMapping("/getAssignmentById/{assignmentId}")
    public ResponseEntity<?> getAssignmentById(@PathVariable Long assignmentId, Authentication authentication) {

        Assignment assignment = assignmentService.getAssignmentById(assignmentId);

        if(assignment == null) return ResponseEntity.badRequest().body("Invalid Assignnment ID");

        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(assignment.getCourse().getId(), authentication);

        if (authResponse != null) {
            return authResponse;
        }

        //Create the DTO for the Assignment Details Object
        AssignmentDetailsDTO assignmentDetailsDTO = new AssignmentDetailsDTO();
        //Setting the details for the assignment details object
        assignmentDetailsDTO.setId( assignment.getId() );
        assignmentDetailsDTO.setTitle(  assignment.getTitle());
        assignmentDetailsDTO.setDescription( assignment.getDescription());
        assignmentDetailsDTO.setDeadline(  assignment.getDeadline() );

        //Create the Assignment Response DTO Object
        AssignmentResponseDTO assignmentResponseDTO = dtoObjectsCreator.createAssignmentResponseDTO( assignmentDetailsDTO, null );

        return ResponseEntity.ok(assignmentResponseDTO);

    }


    // Update an assignment (only for instructors)
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

    @GetMapping("/course/{courseId}/assignments")
    public ResponseEntity<?> getAssignmentsForTheCourse(@PathVariable Long courseId, Authentication authentication) {
        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseId, authentication);

        if (authResponse != null) {
            return authResponse;
        }


        List<AssignmentResponseDTO> assignmentsList = assignmentService.getAssignmentsByCourse(courseId).stream()
                .map(assignment -> {

                    //Create the DTO for the Assignment Details Object
                    AssignmentDetailsDTO assignmentDetailsDTO = new AssignmentDetailsDTO();
                    //Setting the details for the assignment details object
                    assignmentDetailsDTO.setId( assignment.getId() );
                    assignmentDetailsDTO.setTitle(  assignment.getTitle());
                    assignmentDetailsDTO.setDescription( assignment.getDescription());
                    assignmentDetailsDTO.setDeadline(  assignment.getDeadline() );

                    //Create the assignment response dto by calling the DTO object
                    return dtoObjectsCreator.createAssignmentResponseDTO( assignmentDetailsDTO, null );
                }).toList();

        return ResponseEntity.ok(assignmentsList);
    }

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
            Set<Submission> assignmentSubmissions = assignmentDetails.getSubmissions();
            if( assignmentSubmissions.isEmpty() )
                return ResponseEntity.status(200).body("No submissions yet for this assignment");
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
                AssignmentDetailsDTO assignmentDetailsDTO = new AssignmentDetailsDTO();
                //Setting the details for the assignment details object
                assignmentDetailsDTO.setId( assignmentDetails.getId() );
                assignmentDetailsDTO.setTitle(  assignmentDetails.getTitle());
                assignmentDetailsDTO.setDescription( assignmentDetails.getDescription());
                assignmentDetailsDTO.setDeadline(  assignmentDetails.getDeadline() );

                //Create the AssignmentResponse DTO by sending the details
                AssignmentResponseDTO assignmentResponseDTO = dtoObjectsCreator.createAssignmentResponseDTO( assignmentDetailsDTO, submissionResponseDTOList );

                return ResponseEntity.ok( assignmentResponseDTO );
            }

        }
    }

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


    @PostMapping("/GradeBook/GradeAssignment")
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
            else{

                //Construct the new GradeBook object so that it can the submission can be saved
                Gradebook gradeBookSubmission = Gradebook.builder().
                        grade( gradingDetails.getGrade() ).
                        feedback(gradingDetails.getFeedback() )
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

    @PutMapping("/GradeBook/updateGradeAssignment")
    public ResponseEntity<?> updateGradeDetails( @RequestBody GradeBookRequestDTO gradeBookRequestDTO,
                                                 @RequestParam("newGrade") Optional<Integer> newGrade,
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

        //Check if new grade is passed or not
        //Update the value of the existing grade
        newGrade.ifPresent(gradeBookDetails::setGrade);
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
            return ResponseEntity.status(404).body("No Course Materials have been uploaded yet for this course");
        else
        {
            List<CourseMaterialDTO> courseMaterialDTOList = new ArrayList<>();
            courseMaterials.forEach(courseMaterial-> courseMaterialDTOList.add( CourseMaterialDTO.builder().
                    materialTitle( courseMaterial.getTitle() )
                    .materialDescription(courseMaterial.getDescription() ).build()));
            return ResponseEntity.ok(courseMaterialDTOList);
        }
    }

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
        System.out.println( "Deletion Status of existing course material " + isExistingCourseMaterialDeleted );

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


            return new ResponseEntity<>(fileService.updateCourseMaterial( file, instructorData, courseData, courseMaterialDTO, existingCourseMaterial ), HttpStatus.OK);

        }
        else
        {
            return ResponseEntity.status(200).body("Failed to delete the existing course material, Please try again later" );
        }
    }

    @DeleteMapping("/deleteCourseMaterial/{courseId}/{materialTitle}")
    public ResponseEntity<?> deleteCourseMaterial(@PathVariable Long courseId, @PathVariable("materialTitle") String courseMaterialTitle, Authentication authentication)
    {
        String email = authentication.getName();

        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseId, authentication);

        if (authResponse != null) {
            return authResponse;
        }

        //Fetch the corresponding course material details
        CourseMaterial courseMaterial = courseMaterialService.getCourseMaterialByTitle( courseMaterialTitle.strip() );

        //Try deleting the existing file first before removing the file
        boolean isExistingCourseMaterialDeleted = fileService.deleteCourseMaterial( courseMaterial.getCourseMaterialUrl() ).isDeletionSuccessfull();

        if(  isExistingCourseMaterialDeleted )
        {
            return ResponseEntity.status(200).body("Course Material successfully removed from cloud storage");
        }
        else
        {
            return ResponseEntity.status(200).body("Failed to delete the existing course material, Please try again later" );
        }
    }

}
