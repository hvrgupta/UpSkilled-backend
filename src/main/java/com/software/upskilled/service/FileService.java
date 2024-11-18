package com.software.upskilled.service;

import com.software.upskilled.Entity.*;
import com.software.upskilled.dto.CourseMaterialDTO;
import com.software.upskilled.dto.FileDeletionResponse;
import com.software.upskilled.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for handling file operations related to course materials, syllabus, and assignment submissions.
 * Includes methods for uploading, updating, viewing, and deleting files, as well as managing responses related to file uploads and deletions.
 */
public interface FileService {
    FileUploadResponse uploadSyllabus(MultipartFile multipartFile, Long courseId);

    FileUploadResponse uploadCourseMaterial(MultipartFile multipartFile, String instructorName, String courseTitle, CourseMaterialDTO courseMaterialDTO);

    FileUploadResponse updateCourseMaterial(MultipartFile multipartFile, String instructorName, String courseTitle, CourseMaterialDTO courseMaterialDTO, CourseMaterial existingCourseMaterial);

    FileUploadResponse uploadAssignmentSubmission(MultipartFile multipartFile, Course courseData, Assignment assignmentData, Users employeeData );

    FileUploadResponse updateAssignmentSubmission(MultipartFile multipartFile, Submission alreadySubmittedSubmission );

    public FileDeletionResponse deleteCourseMaterial(String courseMaterialURL);

    public FileDeletionResponse deleteUploadedAssignment( String submissionURL );

    public byte[] viewCourseMaterial( String courseMaterialURL);

    public byte[] viewSyllabus(Long courseId);

    public byte[] viewAssignmentSubmission(String assignmentSubmissionURL);
}