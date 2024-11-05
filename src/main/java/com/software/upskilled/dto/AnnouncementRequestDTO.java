package com.software.upskilled.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementRequestDTO {

    @Getter
    private Long id;

    private String title;
    private String content;
    private Date updatedAt;
}
