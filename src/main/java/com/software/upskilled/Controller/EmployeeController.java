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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
    public ResponseEntity<List<CourseInfoDTO>> viewCourses(Authentication authentication) {

        Users user = userService.findUserByEmail(authentication.getName());

        Set<Long> enrolledCourseIds = user.getEnrollments().stream()
                .map(enrollment -> enrollment.getCourse().getId())  // Get the course ID from enrollments
                .collect(Collectors.toSet());

        List<CourseInfoDTO> courseList =  courseService.getAllCourses().stream()
                .filter(course -> course.getStatus().equals(Course.Status.ACTIVE))
                .filter(course -> !enrolledCourseIds.contains(course.getId()))
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

        if (course == null || course.getStatus().equals(Course.Status.INACTIVE)) {
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

    /* Get assignment for the enrolled course by Id */
    @GetMapping("/getAssignmentById/{assignmentId}")
    public ResponseEntity<?> getAssignmentById(@PathVariable Long assignmentId, Authentication authentication) {

        Assignment assignment = assignmentService.getAssignmentById(assignmentId);

        if(assignment == null) return ResponseEntity.badRequest().body("Invalid Assignnment ID");

        ResponseEntity<String> authResponse = employeeCourseAuth.validateEmployeeForCourse(assignment.getCourse().getId(), authentication);

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

}
