package com.software.upskilled.Controller;

import com.software.upskilled.Entity.Announcement;
import com.software.upskilled.Entity.Course;
import com.software.upskilled.Entity.Users;
import com.software.upskilled.dto.AnnouncementDTO;
import com.software.upskilled.dto.FileUploadResponse;
import com.software.upskilled.service.AnnouncementService;
import com.software.upskilled.service.CourseService;
import com.software.upskilled.service.FileService;
import com.software.upskilled.service.UserService;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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

    @GetMapping("/hello")
    public String hello(){
        return "Hello Instructor";
    }

    @PostMapping("/uploadSyllabus/{courseId}")
    public ResponseEntity<?> uploadSyllabus(@RequestParam("file") MultipartFile file, @PathVariable Long courseId, Authentication authentication) {

        if (!Objects.equals(file.getContentType(), "application/pdf")) {
            return ResponseEntity.badRequest().body("Only PDF Files are allowed.");
        }

        String email = authentication.getName();
        Users instructor = userService.findUserByEmail(email);

        Course course = courseService.findCourseById(courseId);

        if (course == null) {
            return ResponseEntity.badRequest().body("Invalid course ID");
        }

        // Check if the instructor is assigned to this course
        if (!course.getInstructor().getId().equals(instructor.getId())) {
            return ResponseEntity.status(403).body("You are not the instructor of this course");
        }

        return new ResponseEntity<>(fileService.uploadSyllabus(file,courseId), HttpStatus.OK);
    }

    // View announcements for a specific course
    @GetMapping("/course/{courseId}/announcements")
    public ResponseEntity<?> viewAnnouncementsForEditing(
            @PathVariable Long courseId, Authentication authentication) {

        // Get the currently authenticated user (instructor)
        String email = authentication.getName();
        Users instructor = userService.findUserByEmail(email);

        Course course = courseService.findCourseById(courseId);

        if (course == null) {
            return ResponseEntity.badRequest().body("Invalid course ID");
        }

        // Check if the instructor is assigned to this course
        if (!course.getInstructor().getId().equals(instructor.getId())) {
            return ResponseEntity.status(403).body("You are not the instructor of this course");
        }

        // Fetch the announcements for the course
        Set<Announcement> announcements = announcementService.getAnnouncementsByCourseId(courseId);

        // Convert announcements to AnnouncementDTO
        List<AnnouncementDTO> announcementDTOs = announcements.stream()
                .map(announcement -> new AnnouncementDTO(announcement.getId(),announcement.getTitle(), announcement.getContent()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(announcementDTOs);
    }

    // Create a new announcement
    @PostMapping("/course/{courseId}/announcement")
    public ResponseEntity<String> createAnnouncement(
            @PathVariable Long courseId,
            @RequestBody AnnouncementDTO announcementDTO,
            Authentication authentication) {

        String email = authentication.getName();
        Users instructor = userService.findUserByEmail(email);

        Course course = courseService.findCourseById(courseId);

        if (course == null) {
            return ResponseEntity.badRequest().body("Invalid course ID");
        }

        if (!course.getInstructor().getId().equals(instructor.getId())) {
            return ResponseEntity.status(403).body("You are not the instructor of this course");
        }

        Announcement announcement = Announcement.builder()
                        .title(announcementDTO.getTitle())
                        .content(announcementDTO.getContent())
                                .course(course).build();

        announcementService.saveAnnouncement(announcement);

        return ResponseEntity.ok("Announcement created successfully");
    }

    // Edit an existing announcement
    @PutMapping("/announcement/{announcementId}")
    public ResponseEntity<String> editAnnouncement(
            @PathVariable Long announcementId,
            @RequestBody AnnouncementDTO announcementDTO,
            Authentication authentication) {

        String email = authentication.getName();
        Users instructor = userService.findUserByEmail(email);

        Announcement announcement = announcementService.findAnnouncementById(announcementId);

        if (announcement == null) {
            return ResponseEntity.badRequest().body("Announcement not found");
        }

        Course course = announcement.getCourse();

        // Check if the instructor is assigned to the course of this announcement
        if (!course.getInstructor().getId().equals(instructor.getId())) {
            return ResponseEntity.status(403).body("You are not the instructor of this course");
        }

        announcement.setTitle(announcementDTO.getTitle());
        announcement.setContent(announcementDTO.getContent());
        announcementService.saveAnnouncement(announcement);

        return ResponseEntity.ok("Announcement updated successfully");
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
