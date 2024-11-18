package com.software.upskilled.repository;

import com.software.upskilled.Entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing Course entities. Provides methods to retrieve
 * courses by title and by instructor ID.
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Course findByTitle(String title);
    List<Course> findByInstructorId(Long id);
}
