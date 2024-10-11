package com.software.upskilled.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.software.upskilled.Entity.Course;
import com.software.upskilled.dto.FileUploadResponse;
import com.software.upskilled.exception.FileUploadException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Value("${aws.s3.accessKey}")
    private String accessKey;

    @Value("${aws.s3.secretKey}")
    private String secretKey;

    private AmazonS3 s3Client;

    @Autowired
    private CourseService courseService;

    @PostConstruct
    private void initialize() {
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(Regions.US_EAST_1)
                .build();
    }

    @Override
    public FileUploadResponse uploadSyllabus(MultipartFile multipartFile, Long courseId) {

        FileUploadResponse fileUploadResponse = new FileUploadResponse();
        String filePath = "";
        Course course = courseService.findCourseById(courseId);
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(multipartFile.getContentType());
            objectMetadata.setContentLength(multipartFile.getSize());
            filePath = "syllabus/"+ course.getTitle() + "/" + multipartFile.getOriginalFilename();
            s3Client.putObject(bucketName, filePath, multipartFile.getInputStream(), objectMetadata);
            fileUploadResponse.setFilePath(filePath);
            fileUploadResponse.setDateTime(LocalDateTime.now());
            course.setSyllabusUrl(filePath);
            courseService.saveCourse(course);
        } catch (IOException e) {
            log.error("Error occurred ==> {}", e.getMessage());
            throw new FileUploadException("Error occurred in file upload ==> "+e.getMessage());
        }
        return fileUploadResponse;
    }

    @Override
    @Async
    public byte[] viewSyllabus(Long courseId) {

        Course course = courseService.findCourseById(courseId);

        try {
            S3Object object = s3Client.getObject(bucketName,course.getSyllabusUrl());
            S3ObjectInputStream objectContent = object.getObjectContent();

            return IOUtils.toByteArray(objectContent);
        } catch (AmazonServiceException | IOException e) {
            throw new IllegalStateException("Failed to download the file", e);
        }

    }
}
