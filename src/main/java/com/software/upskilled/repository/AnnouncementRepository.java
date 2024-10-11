package com.software.upskilled.repository;

import com.software.upskilled.Entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    Set<Announcement> findByCourseId(Long courseId);
}
