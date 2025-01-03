package com.software.upskilled.service;

import com.software.upskilled.Entity.Assignment;
import com.software.upskilled.repository.AssignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing assignments. Provides methods to create, update, retrieve,
 * delete assignments, and fetch assignments by course or deadline.
 */
@Service
public class AssignmentService {

    @Autowired
    AssignmentRepository assignmentRepository;

    public Assignment createAssignment(Assignment assignment) {
        return assignmentRepository.save(assignment);
    }

    public List<Assignment> getAssignmentsByCourse(Long courseId) {
        return assignmentRepository.findByCourseId(courseId);
    }

    public Assignment getAssignmentById(Long id) {
        return assignmentRepository.findById(id).orElse(null);
    }

    public Assignment updateAssignment(Assignment assignment) {
        return assignmentRepository.save(assignment);
    }

    public void deleteAssignment(Long id) {
        assignmentRepository.deleteById(id);
    }

    //Method that returns all the assignment from today
    public List<Assignment> getAllAssignmentsSortedByDeadLine( long courseId ) {
        return assignmentRepository.findAssignmentsSortedByDeadline( courseId );
    }

    @Transactional
    public void deleteAssignmentsByCourseId(Long courseId) {
        assignmentRepository.deleteAllByCourseId(courseId);
    }
}
