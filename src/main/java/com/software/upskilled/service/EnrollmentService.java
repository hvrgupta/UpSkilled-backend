package com.software.upskilled.service;

import com.software.upskilled.Entity.Course;
import com.software.upskilled.Entity.Enrollment;
import com.software.upskilled.Entity.Submission;
import com.software.upskilled.Entity.Users;
import com.software.upskilled.repository.CourseRepository;
import com.software.upskilled.repository.EnrollmentRepository;
import com.software.upskilled.repository.SubmissionRepository;
import com.software.upskilled.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    public String enrollEmployee(Long courseId, Long employeeId) {
        Optional<Course> courseOptional = courseRepository.findById(courseId);
        Optional<Users> employeeOptional = userRepository.findById(employeeId);

        if (courseOptional.isEmpty()) {
            return "Course not found";
        }

        if (employeeOptional.isEmpty()) {
            return "Employee not found";
        }

        Course course = courseOptional.get();
        Users employee = employeeOptional.get();

        // Check if the employee is already enrolled in the course
        boolean alreadyEnrolled = course.getEnrollments().stream()
                .anyMatch(enrollment -> enrollment.getEmployee().getId().equals(employeeId));

        if (alreadyEnrolled) {
            return "Employee is already enrolled in this course";
        }

        Enrollment enrollment = Enrollment.builder()
                .course(course)
                .employee(employee)
                .build();

        enrollmentRepository.save(enrollment);
        return "Enrollment successful";
    }

    @Transactional
    public void unenrollEmployee(Long courseId, Long employeeId) {

        List<Submission> submissions = submissionRepository.findByEmployee_IdAndAssignment_Course_Id(employeeId, courseId);

        submissionRepository.deleteAll(submissions); // Cascades to delete associated Gradebook entries

        enrollmentRepository.deleteByEmployeeIdAndCourseId(employeeId,courseId);
    }
}

