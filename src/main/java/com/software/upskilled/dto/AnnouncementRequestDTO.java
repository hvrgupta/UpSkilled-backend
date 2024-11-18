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
    /**
     * Data Transfer Object (DTO) for carrying announcement request data.
     * This is used for transferring details when creating or updating an announcement.
     */
    @Getter
    private Long id;

    private String title;
    private String content;
    private Date updatedAt;
}
