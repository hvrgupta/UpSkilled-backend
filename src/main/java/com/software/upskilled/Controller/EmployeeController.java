package com.software.upskilled.Controller;

import com.amazonaws.Response;
import com.software.upskilled.Entity.Announcement;
import com.software.upskilled.Entity.Course;
import com.software.upskilled.Entity.CourseMaterial;
import com.software.upskilled.Entity.Users;
import com.software.upskilled.dto.AnnouncementDTO;
import com.software.upskilled.dto.CourseMaterialDTO;
import com.software.upskilled.dto.CourseDTO;
import com.software.upskilled.dto.CourseInfoDTO;
import com.software.upskilled.dto.CreateUserDTO;
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

        List<CourseInfoDTO> courseList =  courseService.getAllCourses().stream().map((course -> {
            CourseInfoDTO courseInfoDTO = new CourseInfoDTO();
            courseInfoDTO.setId(course.getId());
            courseInfoDTO.setTitle(course.getTitle());
            courseInfoDTO.setDescription(course.getDescription());
            courseInfoDTO.setInstructorId(course.getInstructor().getId());
            courseInfoDTO.setInstructorName(course.getInstructor().getFirstName() + " " + course.getInstructor().getLastName());
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

        List<AnnouncementDTO> announcementDTOs = announcements.stream()
                .map(announcement -> new AnnouncementDTO(announcement.getId(),announcement.getTitle(), announcement.getContent()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(announcementDTOs);
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
}
