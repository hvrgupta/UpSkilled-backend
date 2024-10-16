package com.software.upskilled.service;

import com.software.upskilled.Entity.Course;
import com.software.upskilled.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
