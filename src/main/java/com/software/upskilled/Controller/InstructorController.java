package com.software.upskilled.Controller;

import com.software.upskilled.Entity.*;
import com.software.upskilled.dto.*;
import com.software.upskilled.service.*;
import com.software.upskilled.utils.InstructorCourseAuth;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
            return ResponseEntity.badRequest().body("Announcement not found");
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
    public ResponseEntity<String> editAnnouncement(
            @PathVariable Long announcementId,
            @RequestBody AnnouncementDTO announcementDTO,
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

        AssignmentResponseDTO assignmentResponseDTO = new AssignmentResponseDTO();

        assignmentResponseDTO.setId(assignment.getId());
        assignmentResponseDTO.setDescription(assignment.getDescription());
        assignmentResponseDTO.setDeadline(assignment.getDeadline());
        assignmentResponseDTO.setTitle(assignment.getTitle());

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
                    AssignmentResponseDTO assignmentResponseDTO = new AssignmentResponseDTO();
                    assignmentResponseDTO.setTitle(assignment.getTitle());
                    assignmentResponseDTO.setId(assignment.getId());
                    assignmentResponseDTO.setDeadline(assignment.getDeadline());
                    assignmentResponseDTO.setDescription(assignment.getDescription());
                    return assignmentResponseDTO;
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

        //Get the Assignment details by passing the assignmentID
        Assignment assignmentDetails = assignmentService.getAssignmentById( assignmentId );
        //Check if the assignmentDetails is null
        if( assignmentDetails == null ){
            return ResponseEntity.badRequest().body("Assignment not found");
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

                    //Since, our Submission request is linked with other tables, we have to create DTO objects in order
                    //felicitate the transfer of the data,Therefore creating the submission response DTO which will have
                    //the necessary properties
                    SubmissionResponseDTO submissionResponseDTO = new SubmissionResponseDTO();
                    submissionResponseDTO.setSubmission_id( assignmentSubmission.getId() );
                    submissionResponseDTO.setSubmission_url( assignmentSubmission.getSubmissionUrl() );
                    submissionResponseDTO.setSubmission_at( assignmentSubmission.getSubmittedAt() );
                    submissionResponseDTO.setSubmission_status( assignmentSubmission.getStatus() );
                    submissionResponseDTO.setAssignmentID(assignmentDetails.getId());
                    //check if the submission response has a GradeBook
                    if( assignmentSubmission.getGrade() != null )
                    {
                        submissionResponseDTO.setGradeBookId( assignmentSubmission.getGrade().getId() );

                        //Creating  the GradeBook DTO object to populate the grades
                        GradeBookResponseDTO gradeBookResponseDTO = new GradeBookResponseDTO();
                        //Get the Grade object associated with the particular submission
                        Gradebook assignmentSubmissionGradeBook = assignmentSubmission.getGrade();
                        //Adding the details
                        gradeBookResponseDTO.setGrade( assignmentSubmissionGradeBook.getGrade() );
                        gradeBookResponseDTO.setFeedback(assignmentSubmissionGradeBook.getFeedback() );
                        gradeBookResponseDTO.setSubmissionID( assignmentSubmission.getId() );
                        gradeBookResponseDTO.setInstructorID( assignmentSubmissionGradeBook.getInstructor().getId() );
                        gradeBookResponseDTO.setGradedDate( assignmentSubmissionGradeBook.getGradedAt() );

                        //Adding the GradeBook Response DTO to the submission response DTO
                        submissionResponseDTO.setGradeBook( gradeBookResponseDTO );
                    }
                    else
                    {
                        //Setting GradeBook ID as -1 means that no GradeBook Submission exists
                        submissionResponseDTO.setGradeBookId(-1);
                        submissionResponseDTO.setGradeBook( null );
                    }

                    //Adding DTO to the list
                    submissionResponseDTOList.add( submissionResponseDTO );
                });
                return ResponseEntity.ok(submissionResponseDTOList);
            }

        }
    }

    @GetMapping("/{courseID}/assignments/{assignmentId}/submissions/{submissionID}")
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
            return new ResponseEntity<>( fileService.viewAssignmentSubmission( uploadedSubmissionDetails.getSubmissionUrl() ), HttpStatus.OK );
        }
    }

    @GetMapping("/{courseID}/assignments/{assignmentId}/submissions/{submissionID}/GradeBook/getGradeBook")
    public ResponseEntity<?> getParticularSubmissionGradeBook(@PathVariable Long assignmentId, @PathVariable Long courseID,
                                                              @PathVariable Long submissionID, Authentication authentication){
        //Obtaining the email of the user from the authentication object
        String email = authentication.getName();
        //Obtaining the instructor details
        Users instructor = userService.findUserByEmail(email);

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
            Gradebook gradeBookDetails = uploadedSubmissionDetails.getGrade();
            if( gradeBookDetails == null )
                return ResponseEntity.status(200).body("No Grades/Feedback available for this submission yet");
            else
            {
                //Creating the GradeBook Response DTO
                GradeBookResponseDTO gradeBookResponseDTO = new GradeBookResponseDTO();
                gradeBookResponseDTO.setGrade( gradeBookDetails.getGrade() );
                gradeBookResponseDTO.setFeedback( gradeBookDetails.getFeedback() );
                gradeBookResponseDTO.setGradedDate( gradeBookDetails.getGradedAt() );
                gradeBookResponseDTO.setInstructorID( instructor.getId() );
                gradeBookResponseDTO.setSubmissionID( uploadedSubmissionDetails.getId() );

                return ResponseEntity.ok( gradeBookResponseDTO );

            }
        }

    }

    @PostMapping("/GradeBook/GradeAssignment")
    public ResponseEntity<?> submitGradesToGradeBook(@RequestParam("submissionID") String submissionID, @RequestParam("courseID") String courseID, Authentication authentication, @RequestBody GradeBookRequestDTO gradingDetails)
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
                Submission modifiedUploadedSubmissionDetails = submissionService.saveSubmissionDetails( uploadedSubmissionDetails );

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
                                                 @RequestParam("gradingID") long gradingID,
                                                 Authentication authentication)
    {
        //Getting the courseID details from the grading ID
        Gradebook gradeBookDetails = gradeBookService.getGradeBookByID( gradingID );
        //Getting the submission and from there getting the course ID
        long courseID = gradeBookDetails.getSubmission().getAssignment().getCourse().getId();
        //Validating the instructor
        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseID, authentication);
        if (authResponse != null) {
            return authResponse;
        }

        //Updating the values'
        gradeBookDetails.setGrade(gradeBookRequestDTO.getGrade() );
        gradeBookDetails.setFeedback(gradeBookDetails.getFeedback() );

        //Saving the updated value into the database
        gradeBookService.saveGradeBookSubmission( gradeBookDetails );

        //Set the GradeBook Response DT
        GradeBookResponseDTO gradeBookResponseDTO = new GradeBookResponseDTO();
        //Setting the values
        gradeBookResponseDTO.setGrade(gradeBookRequestDTO.getGrade() );
        gradeBookResponseDTO.setFeedback(gradeBookRequestDTO.getFeedback());
        gradeBookResponseDTO.setInstructorID( gradeBookDetails.getInstructor().getId() );
        gradeBookResponseDTO.setSubmissionID( gradeBookDetails.getSubmission().getId() );
        gradeBookResponseDTO.setGradedDate( gradeBookService.getGradeBookByID( gradeBookDetails.getId() ).getGradedAt() );


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

    @GetMapping("/getCourseMaterial/{courseId}/{materialTitle}")
    public ResponseEntity<?> getAllCourseMaterials(@PathVariable Long courseId, @PathVariable("materialTitle") String courseMaterialTitle, Authentication authentication)
    {
        String email = authentication.getName();

        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseId, authentication);

        if (authResponse != null) {
            return authResponse;
        }

        //Fetch the corresponding course material details
        CourseMaterial courseMaterial = courseMaterialService.getCourseMaterialByTitle( courseMaterialTitle.strip() );

        return new ResponseEntity<>(fileService.viewCourseMaterial( courseMaterial.getCourseMaterialUrl() ), HttpStatus.OK);

    }

    @PutMapping("/updateCourseMaterial/{courseId}/{currentMaterialTitle}")
    public ResponseEntity<?> updateCourseMaterial(@RequestParam("file") MultipartFile file, @PathVariable Long courseId,
                                                  @RequestParam("newMaterialTitle") String courseMaterialTitle,
                                                  @RequestParam("newMaterialDescription") String courseMaterialDescription,
                                                  @RequestParam("currentMaterialTitle") String existingCourseMaterialTitle,
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
        CourseMaterial existingCourseMaterial = courseMaterialService.getCourseMaterialByTitle( existingCourseMaterialTitle.strip() );

        String instructorData = instructor.getFirstName()+"_"+instructor.getLastName()+"_"+instructor.getId();
        String courseData = course.getTitle()+"_"+course.getId();

        //Try deleting the existing file first before removing the file
        boolean isExistingCourseMaterialDeleted = fileService.deleteCourseMaterial( existingCourseMaterial.getCourseMaterialUrl() ).isDeletionSuccessfull();
        System.out.println( "Deletion Status of existing course material " + isExistingCourseMaterialDeleted );

        //If the existing course material has been deleted then we proceed to upload the new material.
        if( isExistingCourseMaterialDeleted )
        {
            CourseMaterialDTO newCourseMaterialDTO= CourseMaterialDTO.builder()
                    .materialTitle( courseMaterialTitle )
                    .materialDescription( courseMaterialDescription )
                    .build();
            return new ResponseEntity<>(fileService.updateCourseMaterial( file, instructorData, courseData, newCourseMaterialDTO, existingCourseMaterial ), HttpStatus.OK);

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
