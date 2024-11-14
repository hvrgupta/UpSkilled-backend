package com.software.upskilled.repository;

import com.software.upskilled.Entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCourseId(Long courseId);

    @Query("from Assignment where course.id =:courseId order by deadline asc")
    List<Assignment> findAssignmentsSortedByDeadline(@Param("courseId") long courseId );
}

