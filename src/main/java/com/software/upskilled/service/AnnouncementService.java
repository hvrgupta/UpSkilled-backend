package com.software.upskilled.service;

import com.software.upskilled.Entity.Announcement;
import com.software.upskilled.repository.AnnouncementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

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
}
