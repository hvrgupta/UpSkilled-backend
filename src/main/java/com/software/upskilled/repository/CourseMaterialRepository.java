package com.software.upskilled.repository;

import com.software.upskilled.Entity.CourseMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface CourseMaterialRepository extends JpaRepository<CourseMaterial, Long>
{
    CourseMaterial findByTitle( String courseTitle );

    @Transactional
    @Modifying(flushAutomatically = true)
    @Query("delete from CourseMaterial where id = :courseId")
    void deleteCourseMaterialByCourseId(@Param("courseId") Long courseId);

    List<CourseMaterial> findAllByCourseId(Long courseId);

    void deleteByCourseId(Long courseId);
}
