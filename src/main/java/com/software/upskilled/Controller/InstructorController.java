package com.software.upskilled.Controller;

import com.software.upskilled.Entity.Announcement;
import com.software.upskilled.Entity.Assignment;
import com.software.upskilled.Entity.Course;
import com.software.upskilled.Entity.Users;
import com.software.upskilled.dto.AnnouncementDTO;
import com.software.upskilled.service.*;
import com.software.upskilled.utils.InstructorCourseAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private final InstructorCourseAuth instructorCourseAuth;

    @GetMapping("/hello")
    public String hello(){
        return "Hello Instructor";
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

    @PostMapping("/{courseId}/assignment/create")
    public ResponseEntity<String> createAssignment(@PathVariable Long courseId,
                                                       @RequestBody Assignment assignment,Authentication authentication) {

        String email = authentication.getName();
        Users instructor = userService.findUserByEmail(email);

        Course course = courseService.findCourseById(courseId);
//
//        if (course == null) {
//            return ResponseEntity.badRequest().body("Invalid course ID");
//        }
//
//        // Check if the instructor is assigned to this course
//        if (!course.getInstructor().getId().equals(instructor.getId())) {
//            return ResponseEntity.status(403).body("You are not the instructor of this course");
//        }

        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseId, authentication);
        if (authResponse != null) {
            return authResponse;
        }

        // Set course and creator (instructor)
        assignment.setCourse(course);
        assignment.setCreatedBy(instructor);

        Assignment savedAssignment = assignmentService.createAssignment(assignment);
        return ResponseEntity.ok("Assignment Created successfully.");
    }

    // Update an assignment (only for instructors)
    @PutMapping("/{courseId}/assignment/{assignmentId}")
    public ResponseEntity<String> updateAssignment(@PathVariable Long courseId,
                                                       @PathVariable Long assignmentId,
                                                       @RequestBody Assignment updatedAssignment,
                                                   Authentication authentication) {
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

        Assignment existingAssignment = assignmentService.getAssignmentById(assignmentId);
        if(existingAssignment == null) return ResponseEntity.badRequest().body("Invalid Assignnment ID");

        if(!existingAssignment.getCourse().getId().equals(course.getId())) {
            return ResponseEntity.status(403).body("This assignment doesn't belongs to the course");
        }

        existingAssignment.setTitle(updatedAssignment.getTitle());
        existingAssignment.setDescription(updatedAssignment.getDescription());
        existingAssignment.setDeadline(updatedAssignment.getDeadline());

        Assignment savedAssignment = assignmentService.updateAssignment(existingAssignment);
        return ResponseEntity.ok("Assignment updated successfully");
    }

    // Delete an assignment (only for instructors)
    @DeleteMapping("/{courseId}/assignment/{assignmentId}")
    public ResponseEntity<String> deleteAssignment(@PathVariable Long courseId,
                                                 @PathVariable Long assignmentId,
                                                 Authentication authentication) {

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


        Assignment existingAssignment = assignmentService.getAssignmentById(assignmentId);

        if(existingAssignment == null) return ResponseEntity.badRequest().body("Invalid Assignnment ID");

        // Check if the assignment is assigned to this course
        if(!existingAssignment.getCourse().getId().equals(course.getId())) {
            return ResponseEntity.status(403).body("This assignment doesn't belongs to the course");
        }

        // delete the assignment
        assignmentService.deleteAssignment(assignmentId);

        return ResponseEntity.ok("Assignment Deleted successfully");
    }

}
