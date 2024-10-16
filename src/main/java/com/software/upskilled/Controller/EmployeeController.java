package com.software.upskilled.Controller;

import com.software.upskilled.Entity.Announcement;
import com.software.upskilled.Entity.Course;
import com.software.upskilled.Entity.Users;
import com.software.upskilled.dto.AnnouncementDTO;
import com.software.upskilled.service.AnnouncementService;
import com.software.upskilled.service.CourseService;
import com.software.upskilled.service.FileService;
import com.software.upskilled.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/hello")
    public String hello(){
        return "Hello Employee";
    }

    @PostMapping("/enroll")
    public ResponseEntity<String> enrollInCourse(
            @RequestParam Long courseId,
            Authentication authentication) {

        // Get the currently authenticated user (employee)
        String email = authentication.getName();
        Users employee = userService.findUserByEmail(email);

        // Find the course by its ID
        Course course = courseService.findCourseById(courseId);

        if (course == null) {
            return ResponseEntity.badRequest().body("Invalid course ID");
        }

        // Add employee to the course's enrolledEmployees
        course.getEnrolledEmployees().add(employee);
        courseService.saveCourse(course);

        return ResponseEntity.ok("You have been enrolled in the course: " + course.getTitle());
    }

    @GetMapping("/course/{courseId}/announcements")
    public ResponseEntity<?> viewAnnouncements(
            @PathVariable Long courseId, Authentication authentication) {

        String email = authentication.getName();
        Users employee = userService.findUserByEmail(email);

        Course course = courseService.findCourseById(courseId);

        if (course == null) {
            return ResponseEntity.badRequest().body("Invalid course ID");
        }

        // Check if the employee is enrolled in the course
        if (!course.getEnrolledEmployees().contains(employee)) {
            return ResponseEntity.status(403).body("You are not enrolled in this course");
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
