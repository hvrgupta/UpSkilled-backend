package com.software.upskilled.Controller;

import com.software.upskilled.Entity.Course;
import com.software.upskilled.Entity.Users;
import com.software.upskilled.dto.CourseDTO;
import com.software.upskilled.dto.CourseInfoDTO;
import com.software.upskilled.dto.CreateUserDTO;
import com.software.upskilled.service.FileService;
import com.software.upskilled.service.UserService;
import com.software.upskilled.service.CourseService;
import com.software.upskilled.utils.AdminRoleAuth;
import com.software.upskilled.utils.ErrorResponseMessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private FileService fileService;

    @Autowired
    private AdminRoleAuth adminRoleAuth;

    @Autowired
    private ErrorResponseMessageUtil errorResponseMessageUtil;

    @GetMapping("/hello")
    public String hello(){
        return "Hello Admin";
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

    @GetMapping("/listInstructors")
    public ResponseEntity<List<CreateUserDTO>> getInstructorsList() {

        List<CreateUserDTO> instructorList = new ArrayList<>(userService.getInactiveInstructors().stream().map((instructor) -> {
            CreateUserDTO userDTO = new CreateUserDTO();
            userDTO.setEmail(instructor.getEmail());
            userDTO.setRole(instructor.getRole());
            userDTO.setPassword("*******");
            userDTO.setFirstName(instructor.getFirstName());
            userDTO.setLastName(instructor.getLastName());
            userDTO.setDesignation(instructor.getDesignation());
            userDTO.setId(instructor.getId());
            userDTO.setStatus(instructor.getStatus());
            return userDTO;
        }).toList());
        List<CreateUserDTO> activeInstructorList = userService.getActiveInstructors().stream().map((instructor) -> {
            CreateUserDTO userDTO = new CreateUserDTO();
            userDTO.setEmail(instructor.getEmail());
            userDTO.setRole(instructor.getRole());
            userDTO.setPassword("*******");
            userDTO.setFirstName(instructor.getFirstName());
            userDTO.setLastName(instructor.getLastName());
            userDTO.setDesignation(instructor.getDesignation());
            userDTO.setId(instructor.getId());
            userDTO.setStatus(instructor.getStatus());
            return userDTO;
        }).toList();
        if(instructorList.isEmpty()) {
            return ResponseEntity.ok(activeInstructorList);
        }
        instructorList.addAll(activeInstructorList);
        return ResponseEntity.ok(instructorList);
    }

    @GetMapping("/listActiveInstructors")
    public ResponseEntity<List<CreateUserDTO>> getActiveInstructorsList() {
        List<CreateUserDTO> instructorList = new ArrayList<>(userService.getActiveInstructors().stream().map((instructor) -> {
            CreateUserDTO userDTO = new CreateUserDTO();
            userDTO.setEmail(instructor.getEmail());
            userDTO.setRole(instructor.getRole());
            userDTO.setPassword("*******");
            userDTO.setFirstName(instructor.getFirstName());
            userDTO.setLastName(instructor.getLastName());
            userDTO.setDesignation(instructor.getDesignation());
            userDTO.setId(instructor.getId());
            userDTO.setStatus(instructor.getStatus());
            return userDTO;
        }).toList());

        return ResponseEntity.ok(instructorList);
    }

    @PostMapping("/approve/{instructorId}")
    public ResponseEntity<String> approveInstructor(@PathVariable Long instructorId) {
          Users instructor = userService.findUserById(instructorId);
        if (instructor == null || !instructor.getRole().equals("INSTRUCTOR")) {
            return ResponseEntity.badRequest().body("Invalid instructor ID");
        }
        instructor.setStatus(Users.Status.ACTIVE);
        userService.saveUser(instructor);
        return ResponseEntity.ok("User Approved!");
    }

    @PostMapping("/reject/{instructorId}")
    public ResponseEntity<String> rejectInstructor(@PathVariable Long instructorId) {
        Users instructor = userService.findUserById(instructorId);
        if (instructor == null || !instructor.getRole().equals("INSTRUCTOR")) {
            return ResponseEntity.badRequest().body("Invalid instructor ID");
        }
        instructor.setStatus(Users.Status.REJECTED);
        userService.saveUser(instructor);
        return ResponseEntity.ok("User Rejected!");
    }
/*
    @DeleteMapping("/delete/{instructorId}")
    public ResponseEntity<String> deleteInstructor(@PathVariable Long instructorId) {
        Users instructor = userService.findUserById(instructorId);
        if (instructor == null || !instructor.getRole().equals("INSTRUCTOR")) {
            return ResponseEntity.badRequest().body("Invalid instructor ID");
        }
        userService.deleteUser(instructor);
        return ResponseEntity.ok("User Removed!");
    }
*/
    @PostMapping("/createCourse")
    public ResponseEntity<String> createNewCourse(@RequestBody CourseDTO courseDTO) {
        Users instructor = userService.findUserById(courseDTO.getInstructorId());
        if (instructor == null || (!instructor.getRole().equals("INSTRUCTOR") || !instructor.getStatus().toString().equalsIgnoreCase("ACTIVE"))) {
            return ResponseEntity.badRequest().body("Invalid instructor ID");
        }

        if(courseService.findByTitle(courseDTO.getTitle()) != null) {
            return ResponseEntity.badRequest().body("Course title already exists.");
        }

        if(courseDTO.getTitle().isBlank() || courseDTO.getDescription().isBlank() || courseDTO.getName().isBlank()) {
            return ResponseEntity.badRequest().body("Title, description or name missing!");
        }

        Course newCourse = Course.builder()
                .title(courseDTO.getTitle())
                .description(courseDTO.getDescription())
                .name(courseDTO.getName())
                .instructor(instructor)
                .status(Course.Status.ACTIVE).build();

        courseService.saveCourse(newCourse);

        return ResponseEntity.ok("Course created successfully for instructor: " + instructor.getEmail());
    }

    @PutMapping("/updateCourseDetails/{courseId}")
    public ResponseEntity<?> modifyCourseDetails(@RequestBody CourseDTO courseDTO, @PathVariable Long courseId, @AuthenticationPrincipal Users user) {

        Course course = courseService.findCourseById(courseId);
        if (course == null) {
            return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.BAD_REQUEST.value(), "Course doesn't exist with the particular courseID");
        }

        //Check if the user is actually ADMIN, if not then send error message
        if( !adminRoleAuth.checkUserForAdminRole( user ) )
        {
            return errorResponseMessageUtil.createErrorResponseMessages(HttpStatus.FORBIDDEN.value(), "The user account doesn't have the admin privileges for this operation");
        }

        //Check for the values from the courseDTO
        if( !courseDTO.getTitle().isBlank() )
            //Get the newTitle from the DTO object and set it to the existing course
            course.setTitle( courseDTO.getTitle() );
        if( !courseDTO.getDescription().isBlank() )
            //Get the new Description from the DTO Object and set it to the existing course
            course.setDescription( courseDTO.getDescription() );
        if( !courseDTO.getName().isBlank() )
            //Get the new Name from the DTO Object and set it to existing Name
            course.setName( courseDTO.getName() );
        if( courseDTO.getInstructorId() != 0 ) {
            //Fetch the details of the new Instructor User
            Users instructor = userService.findUserById(courseDTO.getInstructorId());
            //If instructor ID is not null, then perform the change operation
            if (instructor != null) {
                //Save the instructor object to the exiting course details
                course.setInstructor(instructor);
            }
        }

        //Perform the update operation
        courseService.saveCourse(course);

        return ResponseEntity.ok("Course Details updated successfully");
    }

    @PostMapping("/course/inactivate/{courseId}")
    public ResponseEntity<String> inactivateCourse(@PathVariable Long courseId) {
        Course course = courseService.findCourseById(courseId);

        if (course == null) {
            return ResponseEntity.badRequest().body("Course not found.");
        }

        course.setStatus(Course.Status.INACTIVE);
        courseService.saveCourse(course);
        return ResponseEntity.ok("Course Inactivated!");
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
            courseInfoDTO.setName(course.getName());
            courseInfoDTO.setStatus(course.getStatus());
            return courseInfoDTO;
        })).collect(Collectors.toList());
        return ResponseEntity.ok(courseList);
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getCourseDetails(@PathVariable Long courseId) {

        Course course = courseService.findCourseById(courseId);

        if (course == null) {
            return ResponseEntity.badRequest().body("Invalid course ID");
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
                .header("Content-type", "application/octet-stream")
                .header("Content-disposition", "attachment; filename=\"" + syllabusUrl + "\"")
                .body(resource);

    }
}
