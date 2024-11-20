package com.software.upskilled.service;

import com.software.upskilled.Entity.CourseMaterial;
import com.software.upskilled.repository.CourseMaterialRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class CourseMaterialServiceTest
{
    @MockBean
    private CourseMaterialService courseMaterialService;

    @Test
    void saveCourseMaterial_ShouldSaveCourseMaterial() {
        // Arrange
        CourseMaterial courseMaterial = new CourseMaterial();
        courseMaterial.setTitle("Test Material");
        courseMaterial.setDescription("Test Description");
        courseMaterial.setCourseMaterialUrl("http://example.com/material");

        when(courseMaterialService.saveCourseMaterial(courseMaterial)).thenReturn(courseMaterial);

        // Assertion

        CourseMaterial savedCourseMaterial = courseMaterialService.saveCourseMaterial(courseMaterial);

        // Assert
        assertNotNull(savedCourseMaterial);
        assertEquals("Test Material", savedCourseMaterial.getTitle());
        verify(courseMaterialService, times(1)).saveCourseMaterial(courseMaterial);
    }

    @Test
    void updateCourseMaterial() {
        // Arrange
        CourseMaterial courseMaterial = new CourseMaterial();
        courseMaterial.setId(1L);
        courseMaterial.setTitle("Updated Material");
        courseMaterial.setDescription("Updated Description");

        when(courseMaterialService.updateCourseMaterial(courseMaterial)).thenReturn(courseMaterial);

        //Assertion
        CourseMaterial updatedCourseMaterial = courseMaterialService.updateCourseMaterial(courseMaterial);

        // Assert
        assertNotNull(updatedCourseMaterial);
        assertEquals("Updated Material", updatedCourseMaterial.getTitle());
        verify(courseMaterialService, times(1)).updateCourseMaterial(courseMaterial);
    }

    @Test
    void deleteCourseMaterial() {
        // Arrange
        Long courseMaterialId = 1L;
        doNothing().when(courseMaterialService).deleteCourseMaterial(courseMaterialId);

        // Act
        courseMaterialService.deleteCourseMaterial(courseMaterialId);

        // Assert
        verify(courseMaterialService, times(1)).deleteCourseMaterial(courseMaterialId);
    }

    @Test
    void getCourseMaterialById() {
        // Arrange
        Long courseMaterialId = 1L;
        CourseMaterial courseMaterial = new CourseMaterial();
        courseMaterial.setId(courseMaterialId);
        courseMaterial.setTitle("Test Material");

        when(courseMaterialService.getCourseMaterialById(courseMaterialId)).thenReturn( courseMaterial );

        // Act
        CourseMaterial result = courseMaterialService.getCourseMaterialById(courseMaterialId);

        // Assert
        assertNotNull(result);
        assertEquals(courseMaterialId, result.getId());
        verify(courseMaterialService, times(1)).getCourseMaterialById( courseMaterialId );
    }

    @Test
    void getAllCourseMaterialsByCourseId() {
        // Arrange
        Long courseId = 1L;
        CourseMaterial courseMaterial1 = new CourseMaterial();
        courseMaterial1.setTitle("Material 1");
        CourseMaterial courseMaterial2 = new CourseMaterial();
        courseMaterial2.setTitle("Material 2");

        List<CourseMaterial> courseMaterials = List.of(courseMaterial1, courseMaterial2);
        when(courseMaterialService.getAllCourseMaterialsByCourseId( courseId )).thenReturn( courseMaterials );

        //Assertion
        List<CourseMaterial> result = courseMaterialService.getAllCourseMaterialsByCourseId(courseId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Material 1", result.get(0).getTitle());
        verify(courseMaterialService, times(1)).getAllCourseMaterialsByCourseId(courseId);
    }

    @Test
    void deleteCourseMaterialsByCourseId() {
        // Arrange
        Long courseId = 1L;
        doNothing().when(courseMaterialService).deleteCourseMaterialsByCourseId(courseId);

        // Assertion
        courseMaterialService.deleteCourseMaterialsByCourseId(courseId);

        // Assert
        verify(courseMaterialService, times(1)).deleteCourseMaterialsByCourseId(courseId);
    }


}
