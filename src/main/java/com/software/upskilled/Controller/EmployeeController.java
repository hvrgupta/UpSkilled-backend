package com.software.upskilled.Controller;

import com.software.upskilled.Entity.*;
import com.software.upskilled.dto.CourseMaterialDTO;
import com.software.upskilled.dto.CourseInfoDTO;
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
    private MessageService messageService;
    
    @Autowired
    private EmployeeCourseAuth employeeCourseAuth;

    @Autowired
    private GradeBookService gradeBookService;

    @Autowired
    private CreateDTOObjectsImpl dtoObjectsCreator;

    @Autowired
    private ErrorResponseMessageUtil errorResponseMessageUtil;

    @Autowired
    private CoursePropertyValidator coursePropertyValidator;

    @Autowired
    private SubmissionService submissionService;

    /**
     * This endpoint retrieves a list of all active courses that the authenticated user is not enrolled in.
     *
     * The method first fetches the authenticated user based on their email. It then collects the IDs of the courses
     * that the user is currently enrolled in. The list of all active courses is retrieved, and any courses the user is
     * already enrolled in are excluded. The remaining courses are sorted by their update time in descending order. For
     * each course, a CourseInfoDTO is created containing relevant details such as the course ID, title, description,
     * instructor details, and course status. The resulting list of CourseInfoDTO objects is returned as the response.
     *
     * @param authentication The authentication information of the current user.
     * @return A ResponseEntity containing a list of active courses that the user is not enrolled in, represented
     *         by CourseInfoDTO objects.
     */
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

    /**
     * This endpoint retrieves a list of all active courses that the authenticated user (employee) is enrolled in.
     *
     * The method first fetches the authenticated user based on their email. It then iterates through the user's
     * enrollments to get the list of courses they are enrolled in. Only the active courses are included in the response.
     * The courses are sorted by their update time in descending order. For each course, a CourseInfoDTO is created,
     * which contains details such as the course ID, title, description, instructor details, and course status. The
     * resulting list of CourseInfoDTO objects is returned in the response.
     *
     * @param authentication The authentication information of the current user (employee).
     * @return A ResponseEntity containing a list of active courses that the user is enrolled in, represented by
     *         CourseInfoDTO objects.
     */
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

    /**
     * This endpoint retrieves the details of a specific course identified by its course ID.
     *
     * The method first fetches the course from the database using the provided course ID. It then creates a CourseInfoDTO
     * object to map the relevant details of the course, including the course ID, title, description, instructor details,
     * course name, and status. The resulting CourseInfoDTO object is returned as part of the response.
     *
     * @param courseId The ID of the course whose details are being retrieved.
     * @return A ResponseEntity containing the course details represented by a CourseInfoDTO object.
     */
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

    /**
     * This endpoint checks whether the authenticated user (employee) is enrolled in a specific course.
     *
     * The method first retrieves the authenticated user's details based on their email. It then fetches the course
     * associated with the provided course ID. If the course is not found or is inactive, an error message is returned.
     * Otherwise, the method checks if the employee is enrolled in the course. If the employee is not enrolled, a message
     * indicating "Unenrolled" is returned. If the employee is enrolled, a message indicating "Enrolled" is returned.
     *
     * @param courseId The ID of the course to check enrollment for.
     * @param authentication The authentication information of the current user (employee).
     * @return A ResponseEntity containing a message indicating whether the employee is enrolled or not.
     */
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

    /**
     * This endpoint allows the authenticated user (employee) to enroll in a specific course.
     *
     * The method retrieves the authenticated user's details based on their email and fetches the course associated
     * with the provided course ID. If the course is not found or is inactive, an error message is returned. If the
     * course is valid, the enrollment service is called to enroll the employee in the course, and a success message
     * is returned upon successful enrollment.
     *
     * @param courseId The ID of the course the user wishes to enroll in.
     * @param authentication The authentication information of the current user (employee).
     * @return A ResponseEntity containing a success message upon successful enrollment or an error message for invalid input.
     */
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

    /**
     * This endpoint allows the authenticated user (employee) to unenroll from a specific course.
     *
     * The method first validates whether the user is enrolled in the specified course by calling the
     * employeeCourseAuth service. If the validation fails, an error response is returned. If the validation
     * succeeds, the user's details are fetched based on their email, and the enrollment service is called to
     * unenroll the user from the course. A success message is returned upon successful unenrollment.
     *
     * @param courseId The ID of the course the user wishes to unenroll from.
     * @param authentication The authentication information of the current user (employee).
     * @return A ResponseEntity containing a success message upon successful unenrollment or an error message for validation failure.
     */
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


    /**
     * This endpoint retrieves all announcements for a specific course that the authenticated user (employee) is enrolled in.
     *
     * The method first validates whether the user is authorized to view announcements for the specified course by calling
     * the employeeCourseAuth service. If validation fails, an error response is returned. If validation succeeds, the
     * announcements for the course are fetched and sorted in descending order of their update time. Each announcement is
     * mapped to an AnnouncementRequestDTO containing the announcement's ID, title, content, and update timestamp. The
     * resulting list of announcement DTOs is returned in the response.
     *
     * @param courseId The ID of the course for which announcements are being retrieved.
     * @param authentication The authentication information of the current user (employee).
     * @return A ResponseEntity containing a list of announcements represented by AnnouncementRequestDTO objects.
     */
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

    /**
     * This endpoint retrieves the details of a specific announcement identified by its ID.
     *
     * The method first fetches the announcement from the database using the provided announcement ID. If the
     * announcement does not exist, an error response is returned. If the announcement is found, the associated course
     * is retrieved, and the method validates whether the authenticated user (employee) is authorized to access the course
     * using the employeeCourseAuth service. If validation fails, an error response is returned. If validation succeeds,
     * an AnnouncementRequestDTO is created with the announcement's details, including ID, title, content, and update
     * timestamp, and returned in the response.
     *
     * @param id The ID of the announcement to retrieve.
     * @param authentication The authentication information of the current user (employee).
     * @return A ResponseEntity containing the announcement details represented by an AnnouncementRequestDTO object,
     *         or an error message if the announcement is not found or access is unauthorized.
     */
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
    /**
     * This endpoint allows users to download the syllabus for a specific course.
     *
     * The method first fetches the course details using the provided course ID. If the course does not exist, an
     * error response is returned. If the course exists but does not have a syllabus uploaded, an appropriate error
     * message is returned. If a syllabus is available, the method retrieves the syllabus data as a byte array from
     * the fileService, wraps it in a ByteArrayResource, and returns it as a downloadable PDF file with appropriate
     * headers set for content disposition and content type.
     *
     * @param courseId The ID of the course whose syllabus is to be downloaded.
     * @return A ResponseEntity containing the syllabus as a downloadable PDF file or an error message if the course or
     *         syllabus is invalid or unavailable.
     */
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

    /**
     * This endpoint retrieves all course materials for a specific course that the authenticated user (employee) is enrolled in.
     *
     * The method first validates whether the user is authorized to access the specified course using the employeeCourseAuth
     * service. If validation fails, an error response is returned. If validation succeeds, the method fetches the course
     * and its associated materials. If no materials are available, an empty list is returned. Otherwise, each course material
     * is mapped to a CourseMaterialDTO containing details such as the material ID, title, and description. The resulting list
     * of course material DTOs is returned in the response.
     *
     * @param courseId The ID of the course for which materials are being retrieved.
     * @param authentication The authentication information of the current user (employee).
     * @return A ResponseEntity containing a list of course materials represented by CourseMaterialDTO objects,
     *         or an error message if access is unauthorized.
     */
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
            return ResponseEntity.ok( new ArrayList<>() );
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

    /**
     * This endpoint allows the authenticated user (employee) to download a specific course material for a course they are enrolled in.
     *
     * The method first validates whether the user is authorized to access the specified course using the employeeCourseAuth service.
     * If the validation fails, an error response is returned. If the validation succeeds, the course material is fetched by its ID.
     * The file associated with the course material is then retrieved as a byte array using the fileService. The byte array is wrapped
     * in a ByteArrayResource and returned as a downloadable file. The response includes appropriate headers for content disposition
     * and content type to facilitate file download.
     *
     * @param courseId The ID of the course containing the material.
     * @param courseMaterialId The ID of the specific course material to retrieve.
     * @param authentication The authentication information of the current user (employee).
     * @return A ResponseEntity containing the course material as a downloadable file, or an error message if access is unauthorized
     *         or the material is unavailable.
     */
    @GetMapping("/getCourseMaterial/{courseId}/{courseMaterialId}")
    public ResponseEntity<?> getCourseMaterialById(@PathVariable Long courseId, @PathVariable("courseMaterialId") Long courseMaterialId, Authentication authentication)
    {

        ResponseEntity<String> authResponse = employeeCourseAuth.validateEmployeeForCourse(courseId,authentication);

        if (authResponse != null) {
            return authResponse;
        }

        //Fetch the corresponding course material details
        CourseMaterial courseMaterial = courseMaterialService.getCourseMaterialById( courseMaterialId );

        final byte[] data = fileService.viewCourseMaterial( courseMaterial.getCourseMaterialUrl());

        final ByteArrayResource resource = new ByteArrayResource(data);

        String courseMaterialName = courseMaterial.getCourseMaterialUrl().split("/")[2];

        return ResponseEntity
                .ok()
                .contentLength(data.length)
                .header("Content-disposition", "attachment; filename=\"" + courseMaterialName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);

    }

    /**
     * This endpoint allows an authenticated user (employee) to upload an assignment submission for a specific course and assignment.
     *
     * The method first validates whether the user is authorized to access the specified course using the employeeCourseAuth service.
     * If the validation fails, an error response is returned. If validation succeeds, the user's details are retrieved from the
     * authentication object. The assignment and course details are fetched to ensure the assignment is valid and linked to the correct course.
     * The uploaded file is then stored using the fileService, and a success response with the upload details is returned.
     *
     * @param file The multipart file containing the assignment submission.
     * @param authentication The authentication information of the current user (employee).
     * @param assignmentID The ID of the assignment being submitted.
     * @param courseID The ID of the course to which the assignment belongs.
     * @return A ResponseEntity containing the upload details or an error message if validation or upload fails.
     */
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

    /**
     * This endpoint allows an authenticated user (employee) to update a previously uploaded assignment submission for a specific course.
     *
     * The method first validates whether the user is authorized to access the specified course using the employeeCourseAuth service.
     * If validation fails, an error response is returned. If validation succeeds, the existing submission details are retrieved.
     * If the submission has already been graded, the user is restricted from updating it, and an error response is returned.
     *
     * If the submission is eligible for updating, the previously uploaded file is deleted using the fileService. If the deletion
     * is successful, the new file is uploaded as a replacement, and the updated submission details are returned. If the deletion fails,
     * an appropriate error response is returned.
     *
     * @param submissionID The ID of the existing submission to be updated.
     * @param file The new multipart file to replace the previous submission.
     * @param courseID The ID of the course to which the submission belongs.
     * @param authentication The authentication information of the current user (employee).
     * @return A ResponseEntity containing the updated submission details or an error message if validation, deletion, or update fails.
     */
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
        Submission alreadySubmittedResponse = submissionService.getSubmissionByID(Long.parseLong(submissionID));
        //Check if the submitted Response already has a grade linked with it
        Gradebook gradebookDetails = alreadySubmittedResponse.getGrade();

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
                    AssignmentDetailsDTO assignmentDetailsDTO = dtoObjectsCreator.createAssignmentDetailsDTO( assignment );
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

    /**
     * This endpoint retrieves all assignments for a specific course, along with the submission details of the authenticated user (employee) for each assignment.
     *
     * The method first validates if the authenticated user is authorized to access the specified course using the employeeCourseAuth service.
     * If validation fails, an error response is returned. If validation succeeds, the employee's details are fetched using the authentication object.
     *
     * The assignments for the course are retrieved and sorted by their deadlines. For each assignment, the user's submission details are checked and included
     * if available. The response consists of assignment details and the user's corresponding submission information, if any.
     *
     * @param courseId The ID of the course for which the assignments are to be retrieved.
     * @param authentication The authentication information of the current user (employee).
     * @return A ResponseEntity containing a list of AssignmentResponseDTOs with assignment and submission details, or an error message if validation fails.
     */
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
            AssignmentDetailsDTO assignmentDetailsDTO = dtoObjectsCreator.createAssignmentDetailsDTO( particularAssignment );

            //Create the Assignment Response DTO object
            AssignmentResponseDTO assignmentResponseDTO = dtoObjectsCreator.createAssignmentResponseDTO( assignmentDetailsDTO,
                    Collections.singletonList( submissionResponseDTO ));

            return ResponseEntity.ok( assignmentResponseDTO );
        }
        else {

            //Create the DTO for the Assignment Details Object
            AssignmentDetailsDTO assignmentDetailsDTO = dtoObjectsCreator.createAssignmentDetailsDTO( particularAssignment );

            //Create the Assignment Response DTO object
            AssignmentResponseDTO assignmentResponseDTO = dtoObjectsCreator.createAssignmentResponseDTO( assignmentDetailsDTO, null);

            return ResponseEntity.ok( assignmentResponseDTO );
        }
    }

    /**
     * This endpoint retrieves the submission for a specific assignment for a particular employee in a course.
     *
     * The method first validates if the authenticated user is authorized to access the specified course using the employeeCourseAuth service.
     * If validation fails, an error response is returned. Next, it checks whether the assignment belongs to the specified course and validates its association.
     *
     * If valid, the method retrieves the submission details for the authenticated employee. If the employee has not submitted the assignment,
     * an error message is returned. If a submission exists, the file is retrieved from storage and returned as a downloadable PDF.
     *
     * @param courseId The ID of the course in which the assignment exists.
     * @param assignmentId The ID of the assignment for which the submission is being requested.
     * @param authentication The authentication information of the current user (employee).
     * @return A ResponseEntity containing the assignment submission file if available, or an error message if validation fails or no submission exists.
     */
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

    /**
     * This endpoint retrieves the grades and submission statuses for all assignments in a course for the authenticated employee.
     *
     * The method first validates if the authenticated employee is enrolled in the specified course using the employeeCourseAuth service.
     * If the employee is not authorized for the course, an error response is returned.
     *
     * It then fetches all assignments for the course and checks whether the employee has submitted each assignment. For each assignment,
     * it retrieves the submission details (if available), including the status of the submission and its grade (if graded).
     * If no submission exists for an assignment, the status is set to "PENDING".
     *
     * A list of `EmployeeAssignmentGradeDTO` objects is populated with this data, which includes the assignment ID, assignment name, submission ID,
     * submission status, and grade (if available). The final list is returned as a response.
     *
     * @param courseId The ID of the course for which the grades and assignments are being requested.
     * @param authentication The authentication information of the current user (employee).
     * @return A ResponseEntity containing a list of EmployeeAssignmentGradeDTO objects with the employee's assignment grades and statuses.
     */
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

    /**
     * This endpoint allows an employee (student) to send a message to the instructor of a course.
     *
     * The method first validates if the employee is authorized to send a message for the specified course.
     * If the employee is not enrolled in the course or doesn't have permission to send the message, an error response is returned.
     *
     * It then creates a new message object using the details provided in the `messageRequestDTO`, such as the message content and the recipient (instructor).
     * The message is saved to the database, and a response DTO (`MessageResponseDTO`) is generated using the saved message.
     *
     * Finally, the response containing the message details is returned to the client.
     *
     * @param messageRequestDTO The request body containing the message content, course ID, and recipient information.
     * @param authentication The authentication information of the current user (employee).
     * @return A ResponseEntity containing the response DTO with the details of the sent message.
     */
    @PostMapping("/message/sendMessage")
    public ResponseEntity<?> sendMessageToInstructor( @RequestBody MessageRequestDTO messageRequestDTO, Authentication authentication )
    {
        //Obtain the courseId from the messageRequestDTO object
        Long courseId = messageRequestDTO.getCourseId();
        if( courseId == null )
            return errorResponseMessageUtil.createErrorResponseMessages(HttpStatus.BAD_REQUEST.value(), "CourseID is required in the payload");

        ResponseEntity<String> authResponse = employeeCourseAuth.validateEmployeeForCourse(courseId, authentication);
        //If authResponse is not null, then send the authResponse
        if (authResponse != null) {
            return authResponse;
        }
        //Else get the employee details
        Users employeeDetails = userService.findUserByEmail( authentication.getName() );
        //Get the Course Details so that we can get the instructor name
        Course courseDetails = courseService.findCourseById( courseId );
        //Get the Instructor Details
        Users instructorDetails = courseDetails.getInstructor();
        //Create the message details object
        Message newMessage = Message.builder().content( messageRequestDTO.getMessage() )
                .isRead( false )
                .sender( employeeDetails )
                .recipient( instructorDetails )
                .course( courseDetails )
                .build();
        //Save the Message Details to the Database and get the saved object
        Message savedMessage = messageService.createNewMessage( newMessage );
        //Create the Message Response DTO Object
        MessageResponseDTO messageResponseDTO = dtoObjectsCreator.createMessageResponseDTO( savedMessage );

        //Return Response Entity Object
        return ResponseEntity.ok( messageResponseDTO );
    }

    /**
     * This endpoint retrieves all the messages sent by an employee (student) to the instructor of a specific course.
     *
     * The method first validates if the employee is enrolled in the specified course. If the employee is not enrolled, an error response is returned.
     *
     * If the employee is valid, it fetches the list of messages the employee has sent to the instructor for the given course. If messages exist, they are returned in the response DTO.
     * If no messages are found, an empty list is returned in the response DTO.
     *
     * @param courseId The ID of the course to fetch sent messages for.
     * @param authentication The authentication information of the current user (employee).
     * @return A ResponseEntity containing the response DTO with the list of sent messages.
     */
    @GetMapping( "/course/{courseId}/message/getSentMessages" )
    public ResponseEntity<?> getMessagesSentToInstructor( @PathVariable("courseId") Long courseId, Authentication authentication )
    {
        //Check if the employee belongs to the particular course
        ResponseEntity<String> authResponse = employeeCourseAuth.validateEmployeeForCourse(courseId, authentication);
        //If authResponse is not null, then send the authResponse
        if (authResponse != null) {
            return authResponse;
        }
        //Get the employeeDetails
        Users employeeDetails = userService.findUserByEmail( authentication.getName() );
        //Put the employee details in a map
        Map<String, String> userDetailsObject = new HashMap<>();
        userDetailsObject.put("name", employeeDetails.getFirstName() + " " + employeeDetails.getLastName());
        userDetailsObject.put("email", employeeDetails.getEmail());
        //Fetch the list of the messages sent by the Employee for the particular course
        Optional<List<Message>> optionalSentMessageList = messageService.getAllSentMessagesForEmployee( employeeDetails.getId(), courseId );
        //if the List of Messages is null, create an empty response object and send the response
        if( optionalSentMessageList.isPresent() )
        {
            List<Message> sentMessagesOfEmployee = optionalSentMessageList.get();
            //Get the CourseMessages Response DTO object
            CourseMessagesResponseDTO courseSentMessagesResponseDTO = dtoObjectsCreator.createCourseMessagesResponseDTO( userDetailsObject, sentMessagesOfEmployee );

            //Send the created response DTO object
            return ResponseEntity.ok( courseSentMessagesResponseDTO );
        }
        else
        {
            CourseMessagesResponseDTO courseSentMessagesResponseDTO = dtoObjectsCreator.createCourseMessagesResponseDTO( userDetailsObject, new ArrayList<>() );
            return ResponseEntity.ok( courseSentMessagesResponseDTO );
        }
    }

    /**
     * This endpoint retrieves all the messages received by an employee (student) from the instructor of a specific course.
     *
     * The method first validates if the employee is enrolled in the specified course. If the employee is not enrolled, an error response is returned.
     *
     * If the employee is valid, it fetches the list of messages received by the employee for the given course. If messages exist, they are returned in the response DTO.
     * If no messages are found, an empty list is returned in the response DTO.
     *
     * @param courseId The ID of the course to fetch received messages for.
     * @param authentication The authentication information of the current user (employee).
     * @return A ResponseEntity containing the response DTO with the list of received messages.
     */
    @GetMapping("/course/{courseId}/message/getReceivedMessages")
    public ResponseEntity<?> getMessagesReceivedFromInstructor( @PathVariable Long courseId , Authentication authentication )
    {
        //Check if the employee belongs to the particular course
        ResponseEntity<String> authResponse = employeeCourseAuth.validateEmployeeForCourse(courseId, authentication);
        //If authResponse is not null, then send the authResponse
        if (authResponse != null) {
            return authResponse;
        }
        //Get the employeeDetails
        Users employeeDetails = userService.findUserByEmail( authentication.getName() );
        //Put the employee details in a map
        Map<String, String> userDetailsObject = new HashMap<>();
        userDetailsObject.put("name", employeeDetails.getFirstName() + " " + employeeDetails.getLastName());
        userDetailsObject.put("email", employeeDetails.getEmail());
        //Fetch the list of the messages sent by the Employee for the particular course
        Optional<List<Message>> optionalResponseMessageList = messageService.getAllReceivedMessageForEmployee( employeeDetails.getId(), courseId );
        //if the List of Messages is null, create an empty response object and send the response
        if( optionalResponseMessageList.isPresent() )
        {
            List<Message> receivedMessagesOfEmployee = optionalResponseMessageList.get();
            //Get the CourseMessages Response DTO object
            CourseMessagesResponseDTO courseReceivedMessagesResponseDTO = dtoObjectsCreator.createCourseMessagesResponseDTO( userDetailsObject, receivedMessagesOfEmployee );

            //Send the created response DTO object
            return ResponseEntity.ok( courseReceivedMessagesResponseDTO );
        }
        else
        {
            CourseMessagesResponseDTO courseReceivedMessagesResponseDTO = dtoObjectsCreator.createCourseMessagesResponseDTO( userDetailsObject, new ArrayList<>() );
            return ResponseEntity.ok( courseReceivedMessagesResponseDTO );
        }
    }

    /**
     * This endpoint updates the status of a message to "read" for a particular message and course.
     *
     * It validates whether the employee (student) is authorized to access the specified course and message.
     * If valid, the message status is updated to "read" and the updated message is returned in the response.
     * If the message doesn't exist, a "no content" error response is returned.
     *
     * @param messageId The ID of the message to be marked as read.
     * @param courseId The ID of the course associated with the message.
     * @param authentication The authentication details of the current user (employee).
     * @return A ResponseEntity containing the updated message details or an error response if the message doesn't exist.
     */
    @PutMapping("/message/readMessage")
    public ResponseEntity<?> setMessageStatusToRead( @RequestParam("messageId") Long messageId, @RequestParam("courseId") Long courseId, Authentication authentication )
    {
        //Check if the instructor is the valid instructor for the course
        ResponseEntity<String> authResponse = employeeCourseAuth.validateEmployeeForCourse(courseId, authentication);

        if (authResponse != null) {
            return authResponse;
        }

        //Get the Message Details
        Optional<Message> messageDetails = messageService.getMessageById( messageId );
        //Check if the message doesn't exist
        if (messageDetails.isPresent()) {
            Message messageDetail = messageDetails.get();
            //Set the status of the message to read
            messageDetail.setIsRead( true );
            //Save the details to the database
            messageService.updateReadStatusOfMessage( messageDetail );
            return ResponseEntity.ok(  dtoObjectsCreator.createMessageResponseDTO( messageDetail )  );
        }
        else
        {
            return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.NO_CONTENT.value(), "The particular Message ID doesn't exist");
        }
    }


}
