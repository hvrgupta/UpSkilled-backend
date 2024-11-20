package com.software.upskilled.service;

import com.software.upskilled.Entity.Assignment;
import com.software.upskilled.Entity.Submission;
import com.software.upskilled.Entity.Users;
import com.software.upskilled.repository.SubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class SubmissionServiceTest
{
    @Mock
    private SubmissionRepository submissionRepository;

    @InjectMocks
    private SubmissionService submissionService;

    private Submission submission;
    private Assignment assignment;
    private Users employee;

    @BeforeEach
    void setUp() {
        // Mock Employee
        employee = new Users();
        employee.setId(1L);
        employee.setEmail("employee@test.com");

        // Mock Assignment
        assignment = new Assignment();
        assignment.setId(101L);
        assignment.setTitle("Assignment 101");

        // Mock Submission
        submission = new Submission();
        submission.setId(1L);
        submission.setSubmissionUrl("http://example.com/submission");
        submission.setStatus(Submission.Status.SUBMITTED);
        submission.setAssignment(assignment);
        submission.setEmployee(employee);
    }

    @Test
    void testSaveSubmissionDetails() {
        // Arrange
        when(submissionRepository.save(any(Submission.class))).thenReturn(submission);

        // Act
        Submission savedSubmission = submissionService.saveSubmissionDetails(submission);

        // Assert
        assertNotNull(savedSubmission);
        assertEquals(submission.getId(), savedSubmission.getId());
        assertEquals(submission.getSubmissionUrl(), savedSubmission.getSubmissionUrl());
        verify(submissionRepository, times(1)).save(submission);
    }

    @Test
    void testModifySubmissionDetails() {
        // Arrange
        submission.setSubmissionUrl("http://example.com/modified_submission");
        when(submissionRepository.save(submission)).thenReturn(submission);

        // Act
        Submission modifiedSubmission = submissionService.modifySubmissionDetails(submission);

        // Assert
        assertNotNull(modifiedSubmission);
        assertEquals("http://example.com/modified_submission", modifiedSubmission.getSubmissionUrl());
        verify(submissionRepository, times(1)).save(submission);
    }

    @Test
    void testGetSubmissionById() {
        // Arrange
        when(submissionRepository.getSubmissionById(1L)).thenReturn(submission);

        // Act
        Submission result = submissionService.getSubmissionByID(1L);

        // Assert
        assertNotNull(result);
        assertEquals(submission.getId(), result.getId());
        assertEquals(submission.getSubmissionUrl(), result.getSubmissionUrl());
        verify(submissionRepository, times(1)).getSubmissionById(1L);
    }

    @Test
    void testGetSubmissionsSortedBySubmittedTime() {
        // Arrange
        List<Submission> submissions = List.of(submission);
        when(submissionRepository.getSubmissionsSortedBySubmissionTime(101L)).thenReturn(submissions);

        // Act
        List<Submission> result = submissionService.getSubmissionsSortedBySubmittedTime(101L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(submission.getSubmissionUrl(), result.get(0).getSubmissionUrl());
        verify(submissionRepository, times(1)).getSubmissionsSortedBySubmissionTime(101L);
    }
}
