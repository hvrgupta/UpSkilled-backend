package com.software.upskilled.repository;

import com.software.upskilled.Entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCourseId(Long courseId);

    @Query(value = "select *,from_unixtime( assgmnt.created_at/1000 ) from assignment assgmnt where assgmnt.course_id = :courseID order by deadline desc", nativeQuery = true)
    List<Assignment> findAssignmentsSortedByDeadline( long courseID );
}

