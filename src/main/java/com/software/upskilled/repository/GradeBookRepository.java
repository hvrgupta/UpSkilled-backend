package com.software.upskilled.repository;

import com.software.upskilled.Entity.Gradebook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradeBookRepository extends JpaRepository<Gradebook,Long>
{
}
