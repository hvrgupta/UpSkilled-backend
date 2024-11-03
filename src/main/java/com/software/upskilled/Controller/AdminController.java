package com.software.upskilled.Controller;

import com.software.upskilled.Entity.Course;
import com.software.upskilled.Entity.Users;
import com.software.upskilled.dto.CourseDTO;
import com.software.upskilled.dto.CreateUserDTO;
import com.software.upskilled.service.FileService;
import com.software.upskilled.service.UserService;
import com.software.upskilled.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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

    @GetMapping("/listActiveInstructors")
    public ResponseEntity<List<CreateUserDTO>> getActiveInstructorsList() {
        List<CreateUserDTO> instructorList;
        instructorList = userService.getActiveInstructors().stream().map((instructor) -> {
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
        return ResponseEntity.ok(instructorList);
    }

    @GetMapping("/listInactiveInstructors")
    public ResponseEntity<List<CreateUserDTO>> getInactiveInstructorsList() {
        List<CreateUserDTO> instructorList;
        instructorList = userService.getInactiveInstructors().stream().map((instructor) -> {
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

    @PostMapping("/createCourse")
    public ResponseEntity<String> createNewCourse(@RequestBody CourseDTO courseDTO) {
        Users instructor = userService.findUserById(courseDTO.getInstructorId());
        if (instructor == null || (!instructor.getRole().equals("INSTRUCTOR") || !instructor.getStatus().toString().equalsIgnoreCase("ACTIVE"))) {
            return ResponseEntity.badRequest().body("Invalid instructor ID");
        }

        if(courseService.findByTitle(courseDTO.getTitle()) != null) {
            return ResponseEntity.badRequest().body("Course title already exists.");
        }

        Course newCourse = Course.builder()
                .title(courseDTO.getTitle())
                .description(courseDTO.getDescription())
                .instructor(instructor).build();

        courseService.saveCourse(newCourse);

        return ResponseEntity.ok("Course created successfully for instructor: " + instructor.getEmail());
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
