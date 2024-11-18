package com.software.upskilled.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementDTO {
    /**
     * Data Transfer Object (DTO) for carrying announcement data.
     * Used for transferring announcement details such as title, content, and id.
     */
    @Getter
    private Long id;

    private String title;
    private String content;
}
