package com.software.upskilled.utils;

import com.software.upskilled.Entity.Course;
import com.software.upskilled.Entity.Users;
import com.software.upskilled.service.CourseService;
import com.software.upskilled.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * Component responsible for authenticating and authorizing employees for course access.
 * This class validates whether an employee is allowed to access a specific course.
 */
@Component
public class EmployeeCourseAuth {

    private final UserService userService;
    private final CourseService courseService;

    public EmployeeCourseAuth(UserService userService, CourseService courseService) {
        this.userService = userService;
        this.courseService = courseService;
    }

    /**
     * Validates whether the currently authenticated employee is enrolled in the specified course.
     *
     * This method checks if the course exists and is active, then verifies whether the authenticated user (employee) is enrolled
     * in the course by matching their email with the course's enrollment list. If the course is invalid or the employee is not enrolled,
     * an appropriate error response is returned.
     *
     * @param courseId The ID of the course to validate the employee's enrollment for.
     * @param authentication The authentication object containing the currently authenticated user's details.
     * @return A `ResponseEntity` containing a message indicating the validation result, or `null` if the validation passes.
     */
    public ResponseEntity<String> validateEmployeeForCourse(Long courseId, Authentication authentication) {

        Course course = courseService.findCourseById(courseId);

        if (course == null || course.getStatus().equals(Course.Status.INACTIVE)) {
            return ResponseEntity.badRequest().body("Invalid course ID");
        }

        String email = authentication.getName();
        Users employee = userService.findUserByEmail(email);

        if (course.getEnrollments().stream().noneMatch(enrollment -> enrollment.getEmployee().equals(employee))) {
            return ResponseEntity.status(403).body("You are not enrolled in this course");
        }

        return null;

    }
}
