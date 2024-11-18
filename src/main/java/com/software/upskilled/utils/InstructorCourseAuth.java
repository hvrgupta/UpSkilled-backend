package com.software.upskilled.utils;

import com.software.upskilled.Entity.Course;
import com.software.upskilled.Entity.Users;
import com.software.upskilled.service.CourseService;
import com.software.upskilled.service.UserService;
import lombok.experimental.UtilityClass;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Component responsible for authenticating and authorizing instructors for course access.
 * This class validates whether an instructor is allowed to access and manage a specific course.
 */
@Component
public class InstructorCourseAuth {

    private final UserService userService;
    private final CourseService courseService;

    public InstructorCourseAuth(UserService userService, CourseService courseService) {
        this.userService = userService;
        this.courseService = courseService;
    }

    public ResponseEntity<String> validateInstructorForCourse(Long courseId, Authentication authentication) {
        // Get the currently authenticated user (instructor)
        String email = authentication.getName();
        Users instructor = userService.findUserByEmail(email);

        Course course = courseService.findCourseById(courseId);
        System.out.println( course.getInstructor().getEmail() );

        if(instructor.getStatus().equals(Users.Status.INACTIVE) || instructor.getStatus().equals(Users.Status.REJECTED)) {
            return ResponseEntity.badRequest().body("Instructor not yet ACTIVE.");
        }

        if (course == null || course.getStatus().equals(Course.Status.INACTIVE)){
            return ResponseEntity.badRequest().body("Invalid course ID");
        }

        // Check if the instructor is assigned to this course
        if (!course.getInstructor().getId().equals(instructor.getId())) {
            return ResponseEntity.status(403).body("You are not the instructor of this course");
        }
        return null;
    }
}
