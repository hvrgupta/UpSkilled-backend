package com.software.upskilled.service;

import com.software.upskilled.Entity.Assignment;
import com.software.upskilled.Entity.Course;
import com.software.upskilled.Entity.CourseMaterial;
import com.software.upskilled.Entity.Users;
import com.software.upskilled.dto.CourseMaterialDTO;
import com.software.upskilled.dto.FileDeletionResponse;
import com.software.upskilled.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    FileUploadResponse uploadSyllabus(MultipartFile multipartFile, Long courseId);

    /**
     *
     * @param multipartFile
     * @param instructorName
     * @param courseTitle
     * @return
     */
    FileUploadResponse uploadCourseMaterial(MultipartFile multipartFile, String instructorName, String courseTitle, CourseMaterialDTO courseMaterialDTO);

    FileUploadResponse updateCourseMaterial(MultipartFile multipartFile, String instructorName, String courseTitle, CourseMaterialDTO courseMaterialDTO, CourseMaterial existingCourseMaterial);

    FileUploadResponse uploadAssignmentSubmission(MultipartFile multipartFile, Course courseData, Assignment assignmentData, Users employeeData );

    public FileDeletionResponse deleteCourseMaterial(String courseMaterialURL);

    public byte[] viewCourseMaterial( String courseMaterialURL);

    public byte[] viewSyllabus(Long courseId);

    public byte[] viewAssignmentSubmission(String assignmentSubmissionURL);
}