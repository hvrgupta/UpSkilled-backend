package com.software.upskilled.repository;

import com.software.upskilled.Entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long>
{
    Submission getSubmissionById(long id);

    @Query("from Submission where assignment.id = :assignmentId order by submittedAt desc")
    List<Submission> getSubmissionsSortedBySubmissionTime(@Param("assignmentId") Long assignmentId);

    List<Submission> findByEmployee_IdAndAssignment_Course_Id(Long employeeId, Long courseId);
}
