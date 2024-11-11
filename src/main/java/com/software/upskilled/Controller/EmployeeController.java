package com.software.upskilled.Controller;


import com.software.upskilled.Entity.*;
import com.software.upskilled.dto.CourseMaterialDTO;
import com.software.upskilled.dto.CourseInfoDTO;
import com.software.upskilled.dto.CreateUserDTO;
import com.software.upskilled.Entity.Announcement;
import com.software.upskilled.Entity.Course;
import com.software.upskilled.Entity.CourseMaterial;
import com.software.upskilled.Entity.Users;
import com.software.upskilled.dto.*;
import com.software.upskilled.repository.SubmissionRepository;
import com.software.upskilled.service.*;
import com.software.upskilled.utils.EmployeeCourseAuth;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private FileService fileService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private CourseMaterialService courseMaterialService;

    @Autowired
    private AssignmentService assignmentService;
    
    @Autowired
    private EmployeeCourseAuth employeeCourseAuth;
    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private GradeBookService gradeBookService;

    @GetMapping("/hello")
    public String hello(){
        return "Hello Employee";
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
    public ResponseEntity<List<CourseInfoDTO>> viewCourses() {

        List<CourseInfoDTO> courseList =  courseService.getAllCourses().stream()
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

        ResponseEntity<String> authResponse = employeeCourseAuth.validateEmployeeForCourse(courseId,authentication);

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
    
    @PostMapping("/enroll")
    public ResponseEntity<String> enrollInCourse(
            @RequestParam Long courseId,
            Authentication authentication) {
        String email = authentication.getName();
        Users employee = userService.findUserByEmail(email);
        Course course = courseService.findCourseById(courseId);

        if (course == null) {
            return ResponseEntity.badRequest().body("Invalid course ID");
        }

        return ResponseEntity.ok(enrollmentService.enrollEmployee(courseId, employee.getId()));
    }

    @GetMapping("/course/{courseId}/announcements")
    public ResponseEntity<?> viewAnnouncements(
            @PathVariable Long courseId, Authentication authentication) {

        ResponseEntity<String> authResponse = employeeCourseAuth.validateEmployeeForCourse(courseId,authentication);

        if (authResponse != null) {
            return authResponse;
        }

        // Fetch and return the announcements
        Set<Announcement> announcements = announcementService.getAnnouncementsByCourseId(courseId);

        List<AnnouncementRequestDTO> announcementDTOs = announcements.stream()
                .map(announcement -> new AnnouncementRequestDTO(announcement.getId(),announcement.getTitle(), announcement.getContent(), announcement.getUpdatedAt()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(announcementDTOs);
    }

    @GetMapping("/getAnnouncementById/{id}")
    public ResponseEntity<?> getAnnouncementById(
            @PathVariable Long id, Authentication authentication) {

        Announcement announcement = announcementService.findAnnouncementById(id);

        if (announcement == null) {
            return ResponseEntity.badRequest().body("Announcement not found");
        }

        Course course = announcement.getCourse();

        ResponseEntity<String> authResponse = employeeCourseAuth.validateEmployeeForCourse(course.getId(), authentication);

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

    // Endpoint to view the syllabus for a course
    @GetMapping("/{courseId}/syllabus")
    public ResponseEntity<?> viewSyllabus(@PathVariable Long courseId) {

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

    @GetMapping("/getCourseMaterials/{courseId}")
    public ResponseEntity<?> getAllCourseMaterials(@PathVariable Long courseId, Authentication authentication)
    {
        ResponseEntity<String> authResponse = employeeCourseAuth.validateEmployeeForCourse(courseId,authentication);

        if (authResponse != null) {
            return authResponse;
        }

        Course course = courseService.findCourseById(courseId);

        Set<CourseMaterial> courseMaterials = course.getCourseMaterials();

        if( courseMaterials.isEmpty() )
            return ResponseEntity.status(404).body("No Course Materials have been uploaded yet for this course");
        else
        {
            List<CourseMaterialDTO> courseMaterialDTOList = new ArrayList<>();
            courseMaterials.forEach(courseMaterial->{
                courseMaterialDTOList.add( CourseMaterialDTO.builder().
                        materialTitle( courseMaterial.getTitle() )
                        .materialDescription(courseMaterial.getDescription() ).build());

            });
            return ResponseEntity.ok(courseMaterialDTOList);
        }
    }

    @GetMapping("/getCourseMaterial/{courseId}/{materialTitle}")
    public ResponseEntity<?> getAllCourseMaterials(@PathVariable Long courseId, @PathVariable("materialTitle") String courseMaterialTitle, Authentication authentication)
    {

        ResponseEntity<String> authResponse = employeeCourseAuth.validateEmployeeForCourse(courseId,authentication);

        if (authResponse != null) {
            return authResponse;
        }

        //Fetch the corresponding course material details
        CourseMaterial courseMaterial = courseMaterialService.getCourseMaterialByTitle( courseMaterialTitle.strip() );

        return new ResponseEntity<>(fileService.viewCourseMaterial( courseMaterial.getCourseMaterialUrl() ), HttpStatus.OK);

    }

    @PostMapping("/uploadAssignment")
    public ResponseEntity<?> uploadAssignment(@RequestParam("file") MultipartFile file,
                                              Authentication authentication, @RequestParam("assignmentID") String assignmentID,
                                              @RequestParam("courseID")String courseID)
    {
        ResponseEntity<String> authResponse = employeeCourseAuth.validateEmployeeForCourse(Long.parseLong( courseID ),authentication);

        if (authResponse != null) {
            return authResponse;
        }

        //Get the employee details from the authentication
        String email = authentication.getName();
        Users employee = userService.findUserByEmail(email);

        //Get the assignment details since all the validation checks are successful
        Assignment assignmentDetails = assignmentService.getAssignmentById( Long.parseLong(assignmentID) );
        //Get the course details from the assignment details
        Course course = assignmentDetails.getCourse();

        //Upload the Assignment file to the assignment bucket
        return ResponseEntity.ok( fileService.uploadAssignmentSubmission( file, course, assignmentDetails, employee ) );
    }

    //Method to update the Assignment uploaded. The system always keeps the latest copy of the assignment
    //and removes the previously uploaded file.
    @PutMapping("/updateUploadedAssignment/{submissionID}")
    public ResponseEntity<?> updateUploadedAssignment( @PathVariable("submissionID") String submissionID,
                                                       @RequestParam("file") MultipartFile file,
                                                       @RequestParam("courseID") String courseID,
                                                       Authentication authentication)
    {
        //Invoking the auth util method to verify the employee
        ResponseEntity<String> authResponse = employeeCourseAuth.validateEmployeeForCourse(Long.parseLong( courseID ),authentication);

        if (authResponse != null) {
            return authResponse;
        }
        //Get the already submitted submission details
        Submission alreadySubmittedResponse = submissionRepository.getSubmissionById( Long.parseLong(submissionID) );
        //Check if the submitted Response already has a grade linked with it
        Gradebook gradebookDetails = alreadySubmittedResponse.getGrade();
        System.out.println( gradebookDetails.getGrade() );
        //If the assignment already has a grade, then we need to remove the grade assigned with the submission as well
        if( gradebookDetails != null ){
            gradeBookService.deleteGradeBookSubmission( gradebookDetails.getId() );
        }

        //First delete the already submitted assignment so that the new assignment can be uploaded
        FileDeletionResponse deletionResponse = fileService.deleteUploadedAssignment(alreadySubmittedResponse.getSubmissionUrl() );
        //Check if the file is deleted
        if( deletionResponse.isDeletionSuccessfull() ) {
            //Invoke the update operation on the assignment
            return new ResponseEntity<>( fileService.updateAssignmentSubmission( file, alreadySubmittedResponse ), HttpStatus.OK);
        }
        else
        {
            return ResponseEntity.status(200).body("Failed to delete the existing uploaded Assignment, Please upload again later" );
        }

    }
    /*
        Get Assignments for the enrolled course
    */
    @GetMapping("/course/{courseId}/assignments")
    public ResponseEntity<?> getAssignmentsForTheCourse(@PathVariable Long courseId, Authentication authentication) {
        ResponseEntity<String> authResponse = employeeCourseAuth.validateEmployeeForCourse(courseId, authentication);

        if (authResponse != null) {
            return authResponse;
        }

        //Get the employee details
        Users employeeDetails = userService.findUserByEmail( authentication.getName() );
        List<AssignmentResponseDTO> assignmentsList = assignmentService.getAssignmentsByCourse(courseId).stream()
                .map(assignment ->
                {
                    //Get the submission of the assignment pertaining to the User
                    List<Submission> assignmentSubmissionOfUser = assignment.getSubmissions().stream().filter( assignmentSubmission ->{
                        return Objects.equals(assignmentSubmission.getEmployee().getId(), employeeDetails.getId());
                    }).toList();


                    AssignmentResponseDTO assignmentResponseDTO = new AssignmentResponseDTO();
                    assignmentResponseDTO.setTitle(assignment.getTitle());
                    assignmentResponseDTO.setId(assignment.getId());
                    assignmentResponseDTO.setDeadline(assignment.getDeadline());
                    assignmentResponseDTO.setDescription(assignment.getDescription());

                    //Check if there are any submissions posted by the user for this assignment
                    //If no submissions, then pass -1 to the frontend
                    if( assignmentSubmissionOfUser.isEmpty() )
                        assignmentResponseDTO.setGrade(-1);

                    else
                    {
                        Submission assignmentSubmission = assignmentSubmissionOfUser.get(0);
                        //Check if the submission has not been graded. If the assignment has not been
                        //graded, then send 101 to the frontend.
                        if ( !assignmentSubmission.getStatus().equals( Submission.Status.GRADED ) )
                            assignmentResponseDTO.setGrade(101);
                        else
                            assignmentResponseDTO.setGrade( assignmentSubmission.getGrade().getGrade() );
                    }
                    return assignmentResponseDTO;
                }).toList();

        return ResponseEntity.ok(assignmentsList);
    }


    @GetMapping("/course/{courseId}/assignments/{assignmentId}")
    public ResponseEntity<?> getParticularAssignmentDetails( @PathVariable Long courseId, @PathVariable Long assignmentId, Authentication authentication )
    {
        ResponseEntity<String> authResponse = employeeCourseAuth.validateEmployeeForCourse(courseId, authentication);
        if (authResponse != null) {
            return authResponse;
        }
        //Get the employee details
        Users employeeDetails = userService.findUserByEmail( authentication.getName() );
        //Get the assignment details
        Assignment particularAssignment = assignmentService.getAssignmentById( assignmentId );

        //Check if there are any submissions related to this assignment
        //Get the submission of the assignment pertaining to the User
        List<Submission> assignmentSubmissionOfUser = particularAssignment.getSubmissions().stream().filter( assignmentSubmission ->{
            return Objects.equals(assignmentSubmission.getEmployee().getId(), employeeDetails.getId());
        }).toList();

        AssignmentResponseDTO assignmentResponseDTO = new AssignmentResponseDTO();
        //If submission exist, then get the submission details
        if( !assignmentSubmissionOfUser.isEmpty() )
        {
            Submission assignmentSubmission = assignmentSubmissionOfUser.get(0);
            SubmissionResponseDTO submissionResponseDTO = new SubmissionResponseDTO();
            //Fetch GradeBook Details if the assignment has been graded.
            if( assignmentSubmission.getStatus().equals( Submission.Status.GRADED ) )
            {
                //Get GradeBook for the submission
                Gradebook submissionGradeBook = assignmentSubmission.getGrade();
                //Create the SubmissionResponse GradeBook DTO object
                //Setting the values of the DTO Object
                submissionResponseDTO.setSubmission_id( assignmentSubmission.getId() );
                submissionResponseDTO.setSubmission_url( assignmentSubmission.getSubmissionUrl() );
                submissionResponseDTO.setSubmission_at( assignmentSubmission.getSubmittedAt() );
                submissionResponseDTO.setSubmission_status( assignmentSubmission.getStatus() );
                submissionResponseDTO.setAssignmentID( particularAssignment.getId() );
                submissionResponseDTO.setGradeBookId( submissionGradeBook.getId() );

                //Setting the grade in the assignment dto object
                assignmentResponseDTO.setGrade( submissionGradeBook.getGrade() );

                //Create the GradeBook DTO Object

                GradeBookResponseDTO gradeBookResponseDTO = new GradeBookResponseDTO();

                gradeBookResponseDTO.setGrade( submissionGradeBook.getGrade() );
                gradeBookResponseDTO.setSubmissionID( assignmentSubmission.getId() );
                gradeBookResponseDTO.setFeedback( submissionGradeBook.getFeedback() );
                gradeBookResponseDTO.setInstructorID( submissionGradeBook.getInstructor().getId() );
                gradeBookResponseDTO.setGradedDate( submissionGradeBook.getGradedAt() );

                //Add the GradeBook DTO object to the submission response DTO Object
                submissionResponseDTO.setGradeBook( gradeBookResponseDTO );
            }
            else
            {
                //Setting the values of the DTO Object
                submissionResponseDTO.setSubmission_id( assignmentSubmission.getId() );
                submissionResponseDTO.setSubmission_url( assignmentSubmission.getSubmissionUrl() );
                submissionResponseDTO.setSubmission_at( assignmentSubmission.getSubmittedAt() );
                submissionResponseDTO.setSubmission_status( assignmentSubmission.getStatus() );
                submissionResponseDTO.setAssignmentID( particularAssignment.getId() );
                //-1 indicates that the GradeBook doesn't exist
                submissionResponseDTO.setGradeBookId( -1 );
                submissionResponseDTO.setGradeBook( null );

                //Setting the grade in the assignment dto object
                //Check if the submission has not been graded. If the assignment has not been
                //graded, then send 101 to the frontend.
                assignmentResponseDTO.setGrade( 101 );
            }

            //Setting the values of the assignmentDTO object
            assignmentResponseDTO.setId(particularAssignment.getId() );
            assignmentResponseDTO.setTitle(particularAssignment.getTitle() );
            assignmentResponseDTO.setDescription(particularAssignment.getDescription() );
            assignmentResponseDTO.setDeadline(particularAssignment.getDeadline() );
            assignmentResponseDTO.setSubmissionDetails(Collections.singletonList( submissionResponseDTO ));

            return ResponseEntity.ok( assignmentResponseDTO );
        }
        else {
            //Check if there are any submissions posted by the user for this assignment
            //If no submissions, then pass -1 to the frontend
            //Setting the values of the assignmentDTO object
            assignmentResponseDTO.setId(particularAssignment.getId() );
            assignmentResponseDTO.setTitle(particularAssignment.getTitle() );
            assignmentResponseDTO.setDescription(particularAssignment.getDescription() );
            assignmentResponseDTO.setDeadline(particularAssignment.getDeadline() );
            //Setting the grade in the assignment dto object
            assignmentResponseDTO.setGrade( -1 );
            return ResponseEntity.ok( assignmentResponseDTO );
        }

        //Check if the



    }


}
