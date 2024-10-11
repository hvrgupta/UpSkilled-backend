package com.software.upskilled.Controller;

import com.software.upskilled.Entity.Course;
import com.software.upskilled.Entity.Users;
import com.software.upskilled.dto.CourseDTO;
import com.software.upskilled.service.FileService;
import com.software.upskilled.service.UserService;
import com.software.upskilled.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/createUser")
    ResponseEntity<Long> createUser(@RequestBody Users appUser) {
        Long id = userService.createUser(appUser);
        return ResponseEntity.ok(id);
    }

    @GetMapping("/hello")
    public String hello(){
        return "Hello Admin";
    }

    @PostMapping("/createCourse")
    public ResponseEntity<String> createNewCourse(@RequestBody CourseDTO courseDTO) {
        Users instructor = userService.findUserById(courseDTO.getInstructorId());
        if (instructor == null || !instructor.getRole().equals("INSTRUCTOR")) {
            return ResponseEntity.badRequest().body("Invalid instructor ID");
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
