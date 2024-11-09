package com.software.upskilled.repository;

import com.software.upskilled.Entity.Gradebook;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNullApi;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

public interface GradeBookRepository extends JpaRepository<Gradebook,Long>
{
    @Override
    @Modifying(flushAutomatically = true)
    @Query("delete from Gradebook g where g.id=:gradeBookID")
    void deleteById(@Param("gradeBookID") Long gradeBookID);

    @Override
    Optional<Gradebook> findById(Long gradeBookID);
}
