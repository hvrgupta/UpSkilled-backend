package com.software.upskilled.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CourseDTO {
    private String title;
    private String description;
    private Long instructorId;
}
