package com.software.upskilled.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import com.software.upskilled.Entity.*;
import com.software.upskilled.dto.CourseMaterialDTO;
import com.software.upskilled.dto.FileDeletionResponse;
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
import java.util.Arrays;

@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Value("${aws.s3.bucketName}")
    private String bucketName;

    @Value("${aws.s3.course-materials-bucketName}")
    private String courseMaterialsBucketName;

    @Value("${aws.s3.assignment-bucketName}")
    private String assignmentBucketName;

    @Value("${aws.s3.accessKey}")
    private String accessKey;

    @Value("${aws.s3.secretKey}")
    private String secretKey;

    private AmazonS3 s3Client;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private CourseMaterialService courseMaterialService;

    @Autowired
    private SubmissionService submissionService;

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
    public FileUploadResponse uploadCourseMaterial(MultipartFile multipartFile, String instructorData, String courseData,
                                                   CourseMaterialDTO courseMaterialDetails)
    {
        FileUploadResponse fileUploadResponse = new FileUploadResponse();
        String filePath = "";

        //Getting the courseId; Since the CourseData is in the form of {course-title}_{course-id},using the split
        //to get the course-id
        String [] courseDataArray = courseData.split("_");
        long courseId = Long.parseLong( courseDataArray[1] );
        String courseName = courseDataArray[0];

        //Getting the instructorId; Since the CourseData is in the form of {course-title}_{course-id},using the split
        //to get the course-id
        String [] instructorDataArray = instructorData.split("_");
        String instructorName = instructorDataArray[0]+"_"+instructorDataArray[1];
        long instructorId = Long.parseLong( instructorDataArray[2] );

        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(multipartFile.getContentType());
            objectMetadata.setContentLength(multipartFile.getSize());
            filePath = instructorName + "/"+ courseName + "/" + multipartFile.getOriginalFilename();
            s3Client.putObject(courseMaterialsBucketName, filePath, multipartFile.getInputStream(), objectMetadata);
            fileUploadResponse.setFilePath(filePath);
            fileUploadResponse.setDateTime(LocalDateTime.now());

            /**
             * Creating the courseMaterial object using the builder object notation.
             */
            CourseMaterial courseMaterial = CourseMaterial.builder()
                    .title( courseMaterialDetails.getMaterialTitle() )
                    .description( courseMaterialDetails.getMaterialDescription() )
                    .courseMaterialUrl( filePath )
                    .instructor( userService.findUserById( instructorId ) )
                    .course( courseService.findCourseById( courseId ) ).build();

            //Saving the courseMaterial detail to the database
            courseMaterialService.saveCourseMaterial(courseMaterial);

        } catch (IOException e) {
            log.error("Error occurred ==> {}", e.getMessage());
            throw new FileUploadException("Error occurred in file upload ==> "+e.getMessage());
        }
        return fileUploadResponse;
    }

    @Override
    public FileUploadResponse updateCourseMaterial(MultipartFile multipartFile, String instructorData, String courseData, CourseMaterialDTO courseMaterialDTO, CourseMaterial existingCourseMaterial) {
        FileUploadResponse fileUploadResponse = new FileUploadResponse();
        String filePath = "";

        //Getting the courseId; Since the CourseData is in the form of {course-title}_{course-id},using the split
        //to get the course-id
        String [] courseDataArray = courseData.split("_");
        String courseName = courseDataArray[0];

        //Getting the instructorId; Since the CourseData is in the form of {course-title}_{course-id},using the split
        //to get the course-id
        String [] instructorDataArray = instructorData.split("_");
        String instructorName = instructorDataArray[0]+"_"+instructorDataArray[1];


        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(multipartFile.getContentType());
            objectMetadata.setContentLength(multipartFile.getSize());
            filePath = instructorName + "/"+ courseName + "/" + multipartFile.getOriginalFilename();
            s3Client.putObject(courseMaterialsBucketName, filePath, multipartFile.getInputStream(), objectMetadata);
            fileUploadResponse.setFilePath(filePath);
            fileUploadResponse.setDateTime(LocalDateTime.now());

            /**
             * Updating the existing course material object instead of creating a new one
             */
            existingCourseMaterial.setTitle( courseMaterialDTO.getMaterialTitle() );
            existingCourseMaterial.setDescription( courseMaterialDTO.getMaterialDescription() );
            existingCourseMaterial.setCourseMaterialUrl( filePath );

            //Saving the courseMaterial detail to the database
            courseMaterialService.saveCourseMaterial(existingCourseMaterial);

        } catch (IOException e) {
            log.error("Error occurred ==> {}", e.getMessage());
            throw new FileUploadException("Error occurred in file upload ==> "+e.getMessage());
        }
        return fileUploadResponse;
    }

    @Override
    public FileUploadResponse uploadAssignmentSubmission(MultipartFile multipartFile, Course courseData, Assignment assignmentData, Users employeeData)
    {
        FileUploadResponse fileUploadResponse = new FileUploadResponse();
        String filePath = "";

        //Getting the course name from the course Data
        String courseName = courseData.getTitle();
        //Getting the assignmentName from the assignmentData
        String assignmentName = assignmentData.getTitle();
        //Getting the employee userName from the employeeData
        String employeeName = employeeData.getFirstName() + "_" + employeeData.getLastName();


        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(multipartFile.getContentType());
            objectMetadata.setContentLength(multipartFile.getSize());
            filePath = courseName + "/"+ assignmentName + "/" + employeeName+"_"+multipartFile.getOriginalFilename();
            s3Client.putObject( assignmentBucketName , filePath, multipartFile.getInputStream(), objectMetadata);
            fileUploadResponse.setFilePath(filePath);
            fileUploadResponse.setDateTime(LocalDateTime.now());

            /**
             * Creating the submission object using the builder object notation.
             */
            Submission newSubmissionDetails = Submission.builder()
                    .status(Submission.Status.SUBMITTED)
                    .submissionUrl( filePath )
                    .assignment( assignmentData )
                    .employee( employeeData ).build();

            //Saving the submission details to the database
            submissionService.saveSubmissionDetails( newSubmissionDetails );

        } catch (IOException e) {
            log.error("Error occurred ==> {}", e.getMessage());
            throw new FileUploadException("Error occurred in uploading Assignment File ==> "+e.getMessage());
        }
        return fileUploadResponse;

    }



    @Override
    public FileDeletionResponse deleteCourseMaterial(String courseMaterialURL) {
        try {
            DeleteObjectRequest deleteCourseMaterialRequest = new DeleteObjectRequest( courseMaterialsBucketName, courseMaterialURL );

            s3Client.deleteObject(deleteCourseMaterialRequest);
            System.out.println("Course Material deleted successfully from Cloud Storage.");

            //Creating the File Deletion Response object
            FileDeletionResponse fileDeletionResponse = new FileDeletionResponse();
            fileDeletionResponse.setDeletionSuccessfull( true );
            String [] courseMaterialURLArray = courseMaterialURL.split("_");
            fileDeletionResponse.setFileName( courseMaterialURLArray[2] );

            return fileDeletionResponse;

        } catch (AmazonServiceException e) {
            throw new IllegalStateException("Failed to delete the file", e);
        }
    }

    @Override
    public byte[] viewCourseMaterial( String courseMaterialURL)
    {
        try {
            S3Object object = s3Client.getObject(courseMaterialsBucketName,courseMaterialURL);
            S3ObjectInputStream objectContent = object.getObjectContent();

            return IOUtils.toByteArray(objectContent);
        } catch (AmazonServiceException | IOException e) {
            throw new IllegalStateException("Failed to download the file", e);
        }
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

    @Override
    public byte[] viewAssignmentSubmission(String assignmentSubmissionURL) {
        try {
            S3Object object = s3Client.getObject(assignmentBucketName,assignmentSubmissionURL);
            S3ObjectInputStream objectContent = object.getObjectContent();

            return IOUtils.toByteArray(objectContent);
        } catch (AmazonServiceException | IOException e) {
            throw new IllegalStateException("Failed to download the assignment file", e);
        }
    }
}
