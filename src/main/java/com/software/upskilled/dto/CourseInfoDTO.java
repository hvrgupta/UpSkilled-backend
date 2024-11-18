package com.software.upskilled.dto;

import com.software.upskilled.Entity.Course;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseInfoDTO {
    /**
     * Data Transfer Object (DTO) for representing detailed information about a course.
     * This DTO is used to transfer course details, including course status and instructor information.
     */
    private Long id;
    private String title;
    private String description;
    private String instructorName;
    private Long instructorId;
    private String name;
    private Course.Status status;
}
