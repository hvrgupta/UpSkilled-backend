package com.software.upskilled.service;

import com.software.upskilled.Entity.Course;
import com.software.upskilled.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.List;

import java.util.Set;

/**
 * Service for managing courses. Provides methods to save, retrieve, and delete courses,
 * as well as finding courses by title or instructor ID.
 */
@Service
public class CourseService {
    @Autowired
    private CourseRepository courseRepository;


    public Course saveCourse(Course course) {
        return courseRepository.save(course);
    }

    public Course findCourseById(Long courseId) {
        return courseRepository.findById(courseId).orElse(null);
    }

    public Course findByTitle(String title) { return courseRepository.findByTitle(title); }

    public List<Course> getAllCourses() { return courseRepository.findAll(); }

    public List<Course> findByInstructorId(Long id){ return courseRepository.findByInstructorId(id); }
}
