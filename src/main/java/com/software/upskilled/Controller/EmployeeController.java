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
import com.software.upskilled.utils.CoursePropertyValidator;
import com.software.upskilled.utils.CreateDTOObjectsImpl;
import com.software.upskilled.utils.EmployeeCourseAuth;
import com.software.upskilled.utils.ErrorResponseMessageUtil;
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

    @Autowired
    private CreateDTOObjectsImpl dtoObjectsCreator;
    @Autowired
    private ErrorResponseMessageUtil errorResponseMessageUtil;
    @Autowired
    private CoursePropertyValidator coursePropertyValidator;

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
    public ResponseEntity<List<CourseInfoDTO>> viewCourses(Authentication authentication) {

        Users user = userService.findUserByEmail(authentication.getName());

        Set<Long> enrolledCourseIds = user.getEnrollments().stream()
                .map(enrollment -> enrollment.getCourse().getId())  // Get the course ID from enrollments
                .collect(Collectors.toSet());

        List<CourseInfoDTO> courseList =  courseService.getAllCourses().stream()
                .filter(course -> course.getStatus().equals(Course.Status.ACTIVE))
                .filter(course -> !enrolledCourseIds.contains(course.getId()))
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

    @GetMapping("/enrolledCourses")
    public ResponseEntity<List<CourseInfoDTO>> viewEnrolledCourses(Authentication authentication) {

        String email = authentication.getName();
        Users employee = userService.findUserByEmail(email);

        List<CourseInfoDTO> courseList =  employee.getEnrollments().stream()
                .map(Enrollment::getCourse)
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

    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getCourseDetails(@PathVariable Long courseId) {

        Course course = courseService.findCourseById(courseId);

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

    @GetMapping("/enrollment/{courseId}")
    public ResponseEntity<?> checkEnrollment(@PathVariable Long courseId, Authentication authentication) {
        String email = authentication.getName();
        Users employee = userService.findUserByEmail(email);

        Course course = courseService.findCourseById(courseId);

        if (course == null || course.getStatus().equals(Course.Status.INACTIVE)) {
            return ResponseEntity.badRequest().body("Invalid course ID");
        }

        if (course.getEnrollments().stream().noneMatch(enrollment -> enrollment.getEmployee().equals(employee))) {
            return ResponseEntity.ok("Unenrolled");
        }

        return ResponseEntity.ok("Enrolled");
    }

    
    @PostMapping("/enroll")
    public ResponseEntity<String> enrollInCourse(
            @RequestParam Long courseId,
            Authentication authentication) {
        String email = authentication.getName();
        Users employee = userService.findUserByEmail(email);
        Course course = courseService.findCourseById(courseId);

        if (course == null || course.getStatus().equals(Course.Status.INACTIVE)) {
            return ResponseEntity.badRequest().body("Invalid course ID");
        }

        return ResponseEntity.ok(enrollmentService.enrollEmployee(courseId, employee.getId()));
    }

    @PostMapping("/unenroll/{courseId}")
    public ResponseEntity<String> unenrollFromCourse(@PathVariable Long courseId,
                                                      Authentication authentication){

        ResponseEntity<String> authResponse = employeeCourseAuth.validateEmployeeForCourse(courseId,authentication);

        if (authResponse != null) {
            return authResponse;
        }

        String email = authentication.getName();
        Users employee = userService.findUserByEmail(email);

        enrollmentService.unenrollEmployee(courseId,employee.getId());

        return ResponseEntity.ok("Unenrolled!");
    }

    @GetMapping("/course/{courseId}/announcements")
    public ResponseEntity<?> viewAnnouncements(
            @PathVariable Long courseId, Authentication authentication) {

        ResponseEntity<String> authResponse = employeeCourseAuth.validateEmployeeForCourse(courseId,authentication);

        if (authResponse != null) {
            return authResponse;
        }

        // Fetch and return the announcements sorted by the updated first
        Set<Announcement> announcements = announcementService.getAnnouncementsByCourseId( courseId );

        List<AnnouncementRequestDTO> announcementDTOs = announcements.stream()
                .sorted(Comparator.comparing(Announcement::getUpdatedAt).reversed())
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
                courseMaterialDTOList.add( CourseMaterialDTO.builder()
                                .id(courseMaterial.getId())
                        .materialTitle( courseMaterial.getTitle() )
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
                                              Authentication authentication, @RequestParam("assignmentId") String assignmentID,
                                              @RequestParam("courseId")String courseID)
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
    @PutMapping("/updateUploadedAssignment/{submissionId}")
    public ResponseEntity<?> updateUploadedAssignment( @PathVariable("submissionId") String submissionID,
                                                       @RequestParam("file") MultipartFile file,
                                                       @RequestParam("courseId") String courseID,
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
        //System.out.println( gradebookDetails.getGrade() );
        //If the assignment already has a grade, then we need to remove the grade assigned with the submission as well
        /*if( gradebookDetails != null ){
            gradeBookService.deleteGradeBookSubmission( gradebookDetails.getId() );
            //Change the status of the submitted response to SUBMITTED
            alreadySubmittedResponse.setStatus( Submission.Status.SUBMITTED );
        }*/
        if( gradebookDetails != null )
        {
            return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.BAD_REQUEST.value(), "Employee can not upload assignment past due date");
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
        List<AssignmentResponseDTO> assignmentsList = assignmentService.getAllAssignmentsSortedByDeadLine(courseId)
                .stream().map(assignment ->
                {
                    //Get the submission of the assignment pertaining to the User
                    List<Submission> assignmentSubmissionOfUser = assignment.getSubmissions().stream().filter( assignmentSubmission ->{
                        return Objects.equals(assignmentSubmission.getEmployee().getId(), employeeDetails.getId());
                    }).toList();

                    AssignmentResponseDTO assignmentResponseDTO = new AssignmentResponseDTO();

                    //Create the assignmentDetails DTO Object for the Assignment Response DTO
                    AssignmentDetailsDTO assignmentDetailsDTO = new AssignmentDetailsDTO();
                    //Setting the details from the assignment object
                    assignmentDetailsDTO.setTitle(assignment.getTitle() );
                    assignmentDetailsDTO.setDescription(assignment.getDescription());
                    assignmentDetailsDTO.setDeadline(  assignment.getDeadline() );
                    assignmentDetailsDTO.setId( assignment.getId() );
                    assignmentResponseDTO.setAssignmentDetails( assignmentDetailsDTO );

                    //Check if there are any submissions posted by the user for this assignment
                    //If no submissions, then don't set the Submission Details for the user
                    if( assignmentSubmissionOfUser.isEmpty() )
                        assignmentResponseDTO.setSubmissionDetails( null );
                    else
                    {
                        Submission assignmentSubmission = assignmentSubmissionOfUser.get(0);
                        //Get the Submission Response DTO Object
                        SubmissionResponseDTO submissionResponseDTO = dtoObjectsCreator.createSubmissionDTO( assignmentSubmission, assignment, employeeDetails );
                        //Set the submissionResponse DTO in the assignment Response DTO
                        assignmentResponseDTO.setSubmissionDetails( Collections.singletonList( submissionResponseDTO ) );
                    }
                    //Set the assignmentDetailsDTO in the assignmentResponse DTO

                    return assignmentResponseDTO;
                }).toList();

        return ResponseEntity.ok(assignmentsList);
    }

    @GetMapping("/course/{courseId}/assignments/{assignmentId}")
    public ResponseEntity<?> getAssignmentByID( @PathVariable Long courseId, @PathVariable Long assignmentId, Authentication authentication )
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

        //If submission exist, then get the submission details
        if( !assignmentSubmissionOfUser.isEmpty() )
        {
            Submission assignmentSubmission = assignmentSubmissionOfUser.get(0);
            //Get the Submission Response DTO Object from the DTO Objects creator
            SubmissionResponseDTO submissionResponseDTO = dtoObjectsCreator.createSubmissionDTO( assignmentSubmission, particularAssignment, employeeDetails );

            //Create the DTO for the Assignment Details Object
            AssignmentDetailsDTO assignmentDetailsDTO = new AssignmentDetailsDTO();
            //Setting the details for the assignment details object
            assignmentDetailsDTO.setId( particularAssignment.getId() );
            assignmentDetailsDTO.setTitle(particularAssignment.getTitle());
            assignmentDetailsDTO.setDescription(particularAssignment.getDescription());
            assignmentDetailsDTO.setDeadline(  particularAssignment.getDeadline() );

            //Create the Assignment Response DTO object
            AssignmentResponseDTO assignmentResponseDTO = dtoObjectsCreator.createAssignmentResponseDTO( assignmentDetailsDTO,
                    Collections.singletonList( submissionResponseDTO ));

            return ResponseEntity.ok( assignmentResponseDTO );
        }
        else {

            //Create the DTO for the Assignment Details Object
            AssignmentDetailsDTO assignmentDetailsDTO = new AssignmentDetailsDTO();
            //Setting the details for the assignment details object
            assignmentDetailsDTO.setId( particularAssignment.getId() );
            assignmentDetailsDTO.setTitle(particularAssignment.getTitle());
            assignmentDetailsDTO.setDescription(particularAssignment.getDescription());
            assignmentDetailsDTO.setDeadline(  particularAssignment.getDeadline() );

            //Create the Assignment Response DTO object
            AssignmentResponseDTO assignmentResponseDTO = dtoObjectsCreator.createAssignmentResponseDTO( assignmentDetailsDTO, null);

            return ResponseEntity.ok( assignmentResponseDTO );
        }
    }

    @GetMapping( "/course/{courseId}/assignment/{assignmentId}/viewSubmission" )
    public ResponseEntity<?> getAssignmentSubmissionForEmployee( @PathVariable Long courseId, @PathVariable Long assignmentId, Authentication authentication )
    {
        ResponseEntity<String> authResponse = employeeCourseAuth.validateEmployeeForCourse(courseId, authentication);
        if (authResponse != null) {
            return authResponse;
        }
        //Get the employee details
        Users employeeDetails = userService.findUserByEmail( authentication.getName() );
        //Get the courseDetails
        Course courseDetails = courseService.findCourseById( courseId );
        //Check if the courseDetails is null
        if( courseDetails == null )
            return errorResponseMessageUtil.createErrorResponseMessages(HttpStatus.BAD_REQUEST.value(), "The courseID is invalid");
        //Check if the assignmentID belongs to the courseID
        Map<String,Long> propertyMap = new HashMap<>();
        propertyMap.put( "assignment",assignmentId );
        //If the assignmentId is not the property of the courseID, then send appropriate error message
        if( !coursePropertyValidator.isPropertyOfTheCourse( courseId, propertyMap ) )
            return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.BAD_REQUEST.value(), "The assignmentID doesn't belong to this course Details");

        //Get the assignmentDetails
        Assignment assignmentDetails = assignmentService.getAssignmentById( assignmentId );

        //Check if submission is present for the assignment for the current user
        List<Submission> userSubmission = assignmentDetails.getSubmissions().stream().filter( submission -> submission.getEmployee().getId() == employeeDetails.getId() ).toList();
        //If userSubmission is not there
        if( userSubmission.isEmpty() )
            return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.NO_CONTENT.value(), "No submission exists for this assignment by the employee");
        else
        {
            //Get the Submission object
            Submission submissionDetails = userSubmission.get( 0 );
            //Get the submission url
            String submissionURL = submissionDetails.getSubmissionUrl();
            //Get the file name
            String submissionFileName = submissionURL.split("/")[2];

            final byte[] data = fileService.viewAssignmentSubmission( submissionURL );
            final ByteArrayResource resource = new ByteArrayResource(data);

            return ResponseEntity
                    .ok()
                    .contentLength(data.length)
                    .header("Content-disposition", "attachment; filename=\"" + submissionFileName + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);

        }

    }

    @GetMapping("/getGrades/{courseId}")
    public ResponseEntity<?> getGradesAndAssignments(@PathVariable Long courseId, Authentication authentication) {
        Users employeeDetails = userService.findUserByEmail( authentication.getName());

        ResponseEntity<String> authResponse = employeeCourseAuth.validateEmployeeForCourse(courseId, authentication);

        if (authResponse != null) {
            return authResponse;
        }

        List<Assignment> assignments = assignmentService.getAssignmentsByCourse(courseId);
        List<EmployeeAssignmentGradeDTO> assignmentGradeDTOs = new ArrayList<>();


        for(Assignment assignment: assignments) {
            Optional<Submission> userSubmission = assignment.getSubmissions().stream().filter(submission -> submission.getEmployee().getId().equals(employeeDetails.getId())).findFirst();

            if (userSubmission.isPresent()) {
                Submission submission = userSubmission.get();
                EmployeeAssignmentGradeDTO dto = new EmployeeAssignmentGradeDTO();
                dto.setAssignmentId(assignment.getId());
                dto.setAssignmentName(assignment.getTitle());
                dto.setSubmissionId(submission.getId());
                dto.setStatus(submission.getStatus());

                if(submission.getStatus().equals(Submission.Status.GRADED))
                    dto.setGrade(submission.getGrade().getGrade());

                assignmentGradeDTOs.add(dto);
            }else {
                EmployeeAssignmentGradeDTO dto = new EmployeeAssignmentGradeDTO();
                dto.setAssignmentId(assignment.getId());
                dto.setAssignmentName(assignment.getTitle());
                dto.setStatus(Submission.Status.PENDING);
                assignmentGradeDTOs.add(dto);
            }


        }
        return ResponseEntity.ok(assignmentGradeDTOs);

    }


}
