package com.software.upskilled.service;

import com.software.upskilled.Entity.Assignment;
import com.software.upskilled.repository.AssignmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class AssignmentServiceTest
{
    @MockBean
    AssignmentService assignmentService;

    @Test
    void testCreateAssignment() {
        // Arrange
        Assignment assignment = new Assignment();
        assignment.setTitle("Test Assignment");
        assignment.setDescription("Test Description");
        assignment.setDeadline(System.currentTimeMillis());

        when(assignmentService.createAssignment(assignment)).thenReturn(assignment);

        // Act
        Assignment savedAssignment = assignmentService.createAssignment(assignment);

        // Assert
        assertNotNull(savedAssignment);
        assertEquals("Test Assignment", savedAssignment.getTitle());
        verify(assignmentService, times(1)).createAssignment(assignment);
    }

    @Test
    void testGetAssignmentsByCourse() {
        // Arrange
        Long courseId = 1L;
        Assignment assignment1 = new Assignment();
        assignment1.setTitle("Assignment 1");
        Assignment assignment2 = new Assignment();
        assignment2.setTitle("Assignment 2");

        List<Assignment> assignments = List.of(assignment1, assignment2);
        when(assignmentService.getAssignmentsByCourse(courseId)).thenReturn(assignments);

        // Act
        List<Assignment> result = assignmentService.getAssignmentsByCourse(courseId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Assignment 1", result.get(0).getTitle());
        verify(assignmentService, times(1)).getAssignmentsByCourse(courseId);
    }

    @Test
    void testGetAssignmentById() {
        // Arrange
        Long id = 1L;
        Assignment assignment = new Assignment();
        assignment.setId(id);
        assignment.setTitle("Test Assignment");

        when(assignmentService.getAssignmentById(id)).thenReturn( assignment );

        //Assertion
        Assignment result = assignmentService.getAssignmentById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("Test Assignment", result.getTitle());
        verify(assignmentService, times(1)).getAssignmentById( id );
    }

    @Test
    void testUpdateAssignment() {
        // Arrange
        Assignment assignment = new Assignment();
        assignment.setId(1L);
        assignment.setTitle("Updated Assignment");

        when(assignmentService.updateAssignment(assignment)).thenReturn(assignment);

        // Act
        Assignment updatedAssignment = assignmentService.updateAssignment(assignment);

        // Assert
        assertNotNull(updatedAssignment);
        assertEquals("Updated Assignment", updatedAssignment.getTitle());
        verify(assignmentService, times(1)).updateAssignment(assignment);
    }

    @Test
    void deleteAssignment() {
        // Arrange
        Long id = 1L;
        doNothing().when( assignmentService ).deleteAssignment(id);

        // Act
        assignmentService.deleteAssignment(id);

        // Assert
        verify(assignmentService, times(1)).deleteAssignment(id);
    }

    @Test
    void getAllAssignmentsSortedByDeadLine() {
        // Arrange
        Long courseId = 1L;
        Assignment assignment1 = new Assignment();
        assignment1.setDeadline(System.currentTimeMillis() - 1000);
        Assignment assignment2 = new Assignment();
        assignment2.setDeadline(System.currentTimeMillis());

        List<Assignment> assignments = List.of(assignment2, assignment1);
        when(assignmentService.getAllAssignmentsSortedByDeadLine(courseId)).thenReturn(assignments);

        //Assertion
        List<Assignment> result = assignmentService.getAllAssignmentsSortedByDeadLine(courseId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0).getDeadline() > result.get(1).getDeadline());
        verify(assignmentService, times(1)).getAllAssignmentsSortedByDeadLine(courseId);
    }

    @Test
    void deleteAssignmentsByCourseId() {
        // Arrange
        Long courseId = 1L;
        doNothing().when(assignmentService).deleteAssignmentsByCourseId(courseId);

        // Act
        assignmentService.deleteAssignmentsByCourseId(courseId);

        // Assert
        verify(assignmentService, times(1)).deleteAssignmentsByCourseId(courseId);
    }





}
