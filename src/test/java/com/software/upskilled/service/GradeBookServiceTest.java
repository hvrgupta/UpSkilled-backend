package com.software.upskilled.service;

import com.software.upskilled.Entity.Gradebook;
import com.software.upskilled.Entity.Submission;
import com.software.upskilled.Entity.Users;
import com.software.upskilled.repository.GradeBookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class GradeBookServiceTest
{
    @Mock
    private GradeBookRepository gradeBookRepository;

    @InjectMocks
    private GradeBookService gradeBookService;

    private Gradebook gradebook;

    @BeforeEach
    void setUp() {
        // Set up a sample Gradebook object
        gradebook = new Gradebook();
        gradebook.setId(1L);
        gradebook.setGrade(85);
        gradebook.setFeedback("Well done!");

        Submission submission = new Submission();
        submission.setId(10L);
        gradebook.setSubmission(submission);

        Users instructor = new Users();
        instructor.setId(100L);
        instructor.setFirstName("John");
        instructor.setLastName("Smith");
        gradebook.setInstructor(instructor);
    }

    @Test
    void testSaveGradeBookSubmission() {
        // Arrange
        when(gradeBookRepository.save(any(Gradebook.class))).thenReturn(gradebook);

        // Act
        Gradebook savedGradebook = gradeBookService.saveGradeBookSubmission(gradebook);

        // Assert
        assertNotNull(savedGradebook);
        assertEquals(85, savedGradebook.getGrade());
        assertEquals("Well done!", savedGradebook.getFeedback());
        verify(gradeBookRepository, times(1)).save(any(Gradebook.class));
    }

    @Test
    void testGetGradeBookByID() {
        // Arrange
        when(gradeBookRepository.findById(1L)).thenReturn(Optional.of(gradebook));

        // Act
        Gradebook foundGradebook = gradeBookService.getGradeBookByID(1L);

        // Assert
        assertNotNull(foundGradebook);
        assertEquals(85, foundGradebook.getGrade());
        assertEquals("Well done!", foundGradebook.getFeedback());
        verify(gradeBookRepository, times(1)).findById(1L);
    }

    @Test
    void testDeleteGradeBookSubmission() {
        // Act
        gradeBookService.deleteGradeBookSubmission(1L);

        // Assert
        verify(gradeBookRepository, times(1)).deleteById(1L);
    }
}
