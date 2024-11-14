package com.software.upskilled.repository;

import com.software.upskilled.Entity.Enrollment;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM Enrollment e WHERE e.employee.id = :employeeId AND e.course.id = :courseId")
    void deleteByEmployeeIdAndCourseId(Long employeeId, Long courseId);
}
