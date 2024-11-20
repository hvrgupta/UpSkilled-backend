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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Implementation of the FileService interface for managing file uploads, updates, deletions, and retrievals
 * related to course materials, syllabus, and assignment submissions.
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Value("${aws.s3.bucketName}")
    private String syllabusBucketName;

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

    /**
     * Uploads the syllabus file for a given course to the S3 bucket and updates the course's syllabus URL.
     *
     * This method accepts a syllabus file (`multipartFile`) and associates it with a specific course
     * (identified by `courseId`). The file is uploaded to an S3 bucket with metadata, and the file path
     * is stored in the `syllabusUrl` field of the course. The method returns a `FileUploadResponse` object
     * containing the file path and upload timestamp.
     *
     * @param multipartFile The syllabus file to be uploaded.
     * @param courseId The ID of the course for which the syllabus is being uploaded.
     * @return A `FileUploadResponse` containing the file path and timestamp of the upload.
     * @throws FileUploadException if an error occurs during the file upload process.
     */
    @Override
    public FileUploadResponse uploadSyllabus(MultipartFile multipartFile, Long courseId) {

        FileUploadResponse fileUploadResponse = new FileUploadResponse();
        String filePath = "";
        Course course = courseService.findCourseById(courseId);
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(multipartFile.getContentType());
            objectMetadata.setContentLength(multipartFile.getSize());
            filePath = course.getTitle() + "/" + multipartFile.getOriginalFilename();
            s3Client.putObject(syllabusBucketName, filePath, multipartFile.getInputStream(), objectMetadata);
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

    /**
     * Uploads a course material file to the S3 bucket and saves the corresponding course material details in the database.
     *
     * This method takes in a course material file (`multipartFile`), instructor data (`instructorData`),
     * course data (`courseData`), and course material details (`courseMaterialDetails`). It uploads the file
     * to an S3 bucket, generates a file path based on the instructor's and course's information, and saves the
     * course material details to the database. The method returns a `FileUploadResponse` object containing the
     * file path and the timestamp of the upload.
     *
     * @param multipartFile The course material file to be uploaded.
     * @param instructorData A string containing instructor's name and ID in the format "firstName_lastName_instructorId".
     * @param courseData A string containing the course title and ID in the format "courseTitle_courseId".
     * @param courseMaterialDetails A DTO containing details about the course material (title, description).
     * @return A `FileUploadResponse` containing the file path and timestamp of the upload.
     * @throws FileUploadException if an error occurs during the file upload process.
     */
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
        String instructorName = instructorDataArray[0]+"_"+instructorDataArray[1]+"_"+instructorDataArray[2];
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

    /**
     * Updates an existing course material file in the S3 bucket and updates the corresponding course material details in the database.
     *
     * This method takes in a course material file (`multipartFile`), instructor data (`instructorData`),
     * course data (`courseData`), course material details (`courseMaterialDTO`), and the existing course material
     * (`existingCourseMaterial`). It uploads the new file to the S3 bucket, generates a file path based on the
     * instructor's and course's information, updates the existing course material details, and saves them back
     * into the database. The method returns a `FileUploadResponse` object containing the file path and timestamp of the upload.
     *
     * @param multipartFile The course material file to be uploaded.
     * @param instructorData A string containing instructor's name and ID in the format "firstName_lastName_instructorId".
     * @param courseData A string containing the course title and ID in the format "courseTitle_courseId".
     * @param courseMaterialDTO A DTO containing the updated details of the course material (title, description).
     * @param existingCourseMaterial The existing `CourseMaterial` object to be updated.
     * @return A `FileUploadResponse` containing the updated file path and timestamp of the upload.
     * @throws FileUploadException if an error occurs during the file upload process.
     */
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
        String instructorName = instructorDataArray[0]+"_"+instructorDataArray[1]+"_"+instructorDataArray[2];


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
            courseMaterialService.updateCourseMaterial(existingCourseMaterial);

        } catch (IOException e) {
            log.error("Error occurred ==> {}", e.getMessage());
            throw new FileUploadException("Error occurred in file upload ==> "+e.getMessage());
        }
        return fileUploadResponse;
    }

    /**
     * Uploads an assignment submission file to the S3 bucket and saves the corresponding submission details in the database.
     *
     * This method accepts an assignment submission file (`multipartFile`), course details (`courseData`),
     * assignment details (`assignmentData`), and employee details (`employeeData`). It uploads the file to the S3
     * bucket, generates a file path based on the course, assignment, and employee information, and creates a new
     * submission record in the database. The method returns a `FileUploadResponse` containing the file path and
     * timestamp of the upload.
     *
     * @param multipartFile The assignment submission file to be uploaded.
     * @param courseData The course object containing details like the course title.
     * @param assignmentData The assignment object containing the assignment title.
     * @param employeeData The employee (user) object containing the employee's details.
     * @return A `FileUploadResponse` containing the uploaded file path and timestamp.
     * @throws FileUploadException if an error occurs during the file upload process.
     */
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
        String employeeName = employeeData.getFirstName() + "_" + employeeData.getLastName() + "_" + employeeData.getId();


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

    /**
     * Updates an existing assignment submission by uploading a new file and saving the updated submission details.
     *
     * This method handles the process of updating an already submitted assignment by accepting a new submission file
     * (`multipartFile`) and the existing `Submission` object (`alreadySubmittedSubmission`). It uploads the file to the
     * S3 bucket and updates the corresponding submission URL in the database. The method returns a `FileUploadResponse`
     * containing the file path and the timestamp of the upload.
     *
     * @param multipartFile The new assignment submission file to be uploaded.
     * @param alreadySubmittedSubmission The existing `Submission` object that needs to be updated.
     * @return A `FileUploadResponse` containing the file path and timestamp of the updated submission.
     * @throws FileUploadException if an error occurs during the file upload or update process.
     */
    @Override
    @Transactional
    public FileUploadResponse updateAssignmentSubmission(MultipartFile multipartFile, Submission alreadySubmittedSubmission) {
        FileUploadResponse fileUploadResponse = new FileUploadResponse();
        String filePath = "";

        //Getting the Assignment Details from the already submittedResponse
        Assignment parentAssignment = alreadySubmittedSubmission.getAssignment();
        //Getting the course name from the parent Assignment object
        String courseName = parentAssignment.getCourse().getTitle();
        //Getting the assignment title from the parentAssignment object
        String assignmentName = parentAssignment.getTitle();
        //Getting the employee name who uploaded the assignment
        String employeeName = alreadySubmittedSubmission.getEmployee().getFirstName()+"_"+
                alreadySubmittedSubmission.getEmployee().getLastName() + "_" + alreadySubmittedSubmission.getEmployee().getId();

        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(multipartFile.getContentType());
            objectMetadata.setContentLength(multipartFile.getSize());
            filePath = courseName + "/"+ assignmentName + "/" + employeeName+"_"+multipartFile.getOriginalFilename();
            s3Client.putObject( assignmentBucketName , filePath, multipartFile.getInputStream(), objectMetadata);
            fileUploadResponse.setFilePath(filePath);
            fileUploadResponse.setDateTime(LocalDateTime.now());



            /**
             * Since, we are updating the submission, we just need to upload the submission
             * url and then save the file.
             */
            alreadySubmittedSubmission.setSubmissionUrl( filePath );

            //Saving the new submission details to the database
            submissionService.modifySubmissionDetails( alreadySubmittedSubmission );

        } catch (IOException e) {
            log.error("Error occurred ==> {}", e.getMessage());
            throw new FileUploadException("Error occurred in updating the submitted Assignment File ==> "+e.getMessage());
        }
        return fileUploadResponse;

    }

    /**
     * Asynchronously deletes a course material file from the S3 bucket.
     *
     * This method accepts the URL of a course material file, deletes it from the S3 storage, and returns a response indicating
     * whether the deletion was successful. The file name is extracted from the URL for the response, and if the deletion fails,
     * an exception is thrown.
     *
     * @param courseMaterialURL The URL of the course material file to be deleted from S3 storage.
     * @return A `FileDeletionResponse` indicating the success of the deletion operation and the file name.
     * @throws IllegalStateException if an error occurs during the file deletion process.
     */
    @Override
    @Async
    public FileDeletionResponse deleteCourseMaterial(String courseMaterialURL) {
        try {
            DeleteObjectRequest deleteCourseMaterialRequest = new DeleteObjectRequest( courseMaterialsBucketName, courseMaterialURL );

            s3Client.deleteObject(deleteCourseMaterialRequest);

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

    /**
     * Asynchronously deletes an uploaded assignment file from the S3 bucket.
     *
     * This method accepts the URL of an assignment submission file, deletes it from the S3 storage, and returns a response indicating
     * whether the deletion was successful. The file name is extracted from the URL for the response, and if the deletion fails,
     * an exception is thrown.
     *
     * @param submissionURL The URL of the assignment submission file to be deleted from S3 storage.
     * @return A `FileDeletionResponse` indicating the success of the deletion operation and the file name.
     * @throws IllegalStateException if an error occurs during the file deletion process.
     */
    @Override
    @Async
    public FileDeletionResponse deleteUploadedAssignment(String submissionURL) {
        try {
            DeleteObjectRequest deleteCourseMaterialRequest = new DeleteObjectRequest( assignmentBucketName, submissionURL );

            s3Client.deleteObject(deleteCourseMaterialRequest);

            //Creating the File Deletion Response object
            FileDeletionResponse fileDeletionResponse = new FileDeletionResponse();
            fileDeletionResponse.setDeletionSuccessfull( true );
            String [] submissionURLArray = submissionURL.split("_");
            fileDeletionResponse.setFileName( submissionURLArray[2] );

            return fileDeletionResponse;

        } catch (AmazonServiceException e) {
            throw new IllegalStateException("Failed to delete the uploaded assignment file", e);
        }
    }

//    View buckets

    /**
     * Asynchronously retrieves the content of a course material file from the S3 bucket.
     *
     * This method takes the URL of a course material file, fetches it from the S3 storage, and returns its content as a byte array.
     * If the file retrieval fails due to an error in accessing the file or reading its content, an exception is thrown.
     *
     * @param courseMaterialURL The URL of the course material file to be retrieved from S3 storage.
     * @return A byte array representing the content of the course material file.
     * @throws IllegalStateException if an error occurs during the file download or reading process.
     */
    @Override
    @Async
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

    /**
     * Asynchronously retrieves the syllabus file of a course from the S3 bucket.
     *
     * This method uses the course ID to find the associated course and fetches the syllabus file from S3 storage using the syllabus URL stored in the course object.
     * The syllabus file is returned as a byte array. If there is an issue accessing or reading the file, an exception is thrown.
     *
     * @param courseId The ID of the course whose syllabus file is to be retrieved.
     * @return A byte array representing the content of the syllabus file.
     * @throws IllegalStateException if an error occurs while downloading or reading the file.
     */
    @Override
    @Async
    public byte[] viewSyllabus(Long courseId) {

        Course course = courseService.findCourseById(courseId);

        try {
            S3Object object = s3Client.getObject(syllabusBucketName,course.getSyllabusUrl());
            S3ObjectInputStream objectContent = object.getObjectContent();

            return IOUtils.toByteArray(objectContent);
        } catch (AmazonServiceException | IOException e) {
            throw new IllegalStateException("Failed to download the file", e);
        }

    }

    /**
     * Asynchronously retrieves an assignment submission file from the S3 bucket.
     *
     * This method takes the assignment submission URL and fetches the corresponding file from the S3 storage.
     * The file content is returned as a byte array. If there is an error during file retrieval or reading, an exception is thrown.
     *
     * @param assignmentSubmissionURL The URL of the assignment submission file to be retrieved from S3.
     * @return A byte array representing the content of the assignment submission file.
     * @throws IllegalStateException if an error occurs while downloading or reading the file.
     */
    @Override
    @Async
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
