package com.software.upskilled.service;

import com.software.upskilled.Entity.Announcement;
import com.software.upskilled.Entity.Course;
import com.software.upskilled.Entity.CourseMaterial;
import com.software.upskilled.Entity.Users;
import com.software.upskilled.repository.AnnouncementRepository;
import com.software.upskilled.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class AnnouncementServiceTest
{
    @MockBean
    private AnnouncementService announcementService;

    @Test
    void testSaveAnnouncement() {

        //Set up the details
        Announcement announcement = new Announcement();
        announcement.setTitle("Test Announcement");
        announcement.setContent("Test Content");

        //Set up the behavior
        when( announcementService.saveAnnouncement( announcement ) ).thenReturn( announcement );

        //Test the method under the test

        //Test the method under the test
        Announcement savedAnnouncement = announcementService.saveAnnouncement(announcement);

        // Assert
        assertNotNull(savedAnnouncement);
        assertEquals("Test Announcement", savedAnnouncement.getTitle());
        verify( announcementService ).saveAnnouncement( announcement );
    }

    @Test
    void findAnnouncementById() {
        // Arrange
        Long id = 1L;
        Announcement announcement = new Announcement();
        announcement.setId(id);
        announcement.setTitle("Test Announcement");
        announcement.setContent("Test Content");

        when( announcementService.findAnnouncementById(id) ).thenReturn( announcement );

        // Act
        Announcement foundAnnouncement = announcementService.findAnnouncementById(id);

        // Assert
        assertNotNull(foundAnnouncement);
        assertEquals(id, foundAnnouncement.getId());
        assertEquals("Test Announcement", foundAnnouncement.getTitle());
        verify(announcementService, times(1)).findAnnouncementById(id);
    }

    @Test
    void findAnnouncementSortedByUpdateTime() {
        // Arrange
        Long courseId = 1L;
        Announcement announcement1 = new Announcement();
        announcement1.setTitle("Announcement 1");
        announcement1.setUpdatedAt(new Date(System.currentTimeMillis() - 1000));

        Announcement announcement2 = new Announcement();
        announcement2.setTitle("Announcement 2");
        announcement2.setUpdatedAt(new Date(System.currentTimeMillis()));

        List<Announcement> announcements = List.of(announcement2, announcement1);
        when(announcementService.findAnnouncementSortedByUpdateTime(courseId)).thenReturn(announcements);

        // Act
        List<Announcement> result = announcementService.findAnnouncementSortedByUpdateTime(courseId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Announcement 2", result.get(0).getTitle());
        verify(announcementService, times(1)).findAnnouncementSortedByUpdateTime(courseId);
    }

    @Test
    void deleteAnnouncement() {
        // Arrange
        Long id = 1L;
        doNothing().when(announcementService).deleteAnnouncement(id);

        // Act
        announcementService.deleteAnnouncement(id);

        // Assert
        verify(announcementService, times(1)).deleteAnnouncement(id);
    }

    @Test
    void deleteAnnouncementsByCourseId() {
        // Arrange
        Long courseId = 1L;
        doNothing().when(announcementService).deleteAnnouncementsByCourseId(courseId);

        // Act
        announcementService.deleteAnnouncementsByCourseId(courseId);

        // Assert
        verify(announcementService, times(1)).deleteAnnouncementsByCourseId(courseId);
    }




}
