package com.software.upskilled.repository;

import com.software.upskilled.Entity.CourseMaterial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseMaterialRepository extends JpaRepository<CourseMaterial, Integer>
{
    CourseMaterial findByTitle( String courseTitle );
}
