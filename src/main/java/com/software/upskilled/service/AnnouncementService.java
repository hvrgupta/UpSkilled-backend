package com.software.upskilled.service;

import com.software.upskilled.Entity.Announcement;
import com.software.upskilled.repository.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Service for handling operations related to announcements. Provides methods to save,
 * retrieve, delete announcements, and fetch announcements by course, sorted by update time.
 */
@Service
public class AnnouncementService {
    @Autowired
    private AnnouncementRepository announcementRepository;

    public Announcement saveAnnouncement(Announcement announcement) {
        return announcementRepository.save(announcement);
    }

    public Announcement findAnnouncementById(Long id) {
        return announcementRepository.findById(id).orElse(null);
    }

    public Set<Announcement> getAnnouncementsByCourseId(Long courseId) {
        return announcementRepository.findByCourseId(courseId);
    }

    public List<Announcement> findAnnouncementSortedByUpdateTime( Long courseId )
    {
        return announcementRepository.getAnnouncementsSortedByUpdateTime( courseId );
    }

    public void deleteAnnouncement(Long id) {
        announcementRepository.deleteById(id);
    }

    @Transactional
    public void deleteAnnouncementsByCourseId(Long courseId) {
        announcementRepository.deleteByCourseId(courseId);
    }
}
