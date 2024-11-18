package com.software.upskilled.repository;

import com.software.upskilled.Entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * Repository for managing Announcement entities. Provides methods to
 * retrieve, delete, and sort announcements by course.
 */
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    Set<Announcement> findByCourseId(Long courseId);

    @Query("from Announcement where course.id = :courseId order by updatedAt desc")
    List<Announcement> getAnnouncementsSortedByUpdateTime(@Param("courseId") Long courseId);

    void deleteByCourseId(Long courseId);
}
