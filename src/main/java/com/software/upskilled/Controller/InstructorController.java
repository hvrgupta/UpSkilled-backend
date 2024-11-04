package com.software.upskilled.Controller;

import com.software.upskilled.Entity.*;
import com.software.upskilled.dto.AnnouncementDTO;
import com.software.upskilled.dto.CourseMaterialDTO;
import com.software.upskilled.dto.CreateUserDTO;
import com.software.upskilled.service.*;
import com.software.upskilled.utils.InstructorCourseAuth;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/instructor")
public class InstructorController {

    private final FileService fileService;

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private UserService userService;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private final InstructorCourseAuth instructorCourseAuth;

    @Autowired
    private final CourseMaterialService courseMaterialService;

    @Autowired
    private SubmissionService submissionService;


    @GetMapping("/hello")
    public String hello(){
        return "Hello Instructor";
    }

    @GetMapping("/me")
    public CreateUserDTO getCurrentUser(@AuthenticationPrincipal Users user) {
        CreateUserDTO userDTO = new CreateUserDTO();
        userDTO.setId(user.getId());
        userDTO.setEmail(user.getEmail());
        userDTO.setRole(user.getRole());
        userDTO.setPassword("*******");
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setDesignation(user.getDesignation());
        userDTO.setStatus(user.getStatus());
        return userDTO;
    }

    // View announcements for a specific course
    @GetMapping("/course/{courseId}/announcements")
    public ResponseEntity<?> viewAnnouncementsForEditing(
            @PathVariable Long courseId, Authentication authentication) {

        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseId, authentication);

        if (authResponse != null) {
            return authResponse;
        }

        // Fetch the announcements for the course
        Set<Announcement> announcements = announcementService.getAnnouncementsByCourseId(courseId);

        // Convert announcements to AnnouncementDTO
        List<AnnouncementDTO> announcementDTOs = announcements.stream()
                .map(announcement -> new AnnouncementDTO(announcement.getId(),announcement.getTitle(), announcement.getContent()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(announcementDTOs);
    }

    // Create a new announcement
    @PostMapping("/course/{courseId}/announcement")
    public ResponseEntity<String> createAnnouncement(
            @PathVariable Long courseId,
            @RequestBody AnnouncementDTO announcementDTO,
            Authentication authentication) {

        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseId, authentication);

        if (authResponse != null) {
            return authResponse;
        }

        Course course = courseService.findCourseById(courseId);

        Announcement announcement = Announcement.builder()
                        .title(announcementDTO.getTitle())
                        .content(announcementDTO.getContent())
                                .course(course).build();

        announcementService.saveAnnouncement(announcement);

        return ResponseEntity.ok("Announcement created successfully");
    }

    // Edit an existing announcement
    @PutMapping("/announcement/{announcementId}")
    public ResponseEntity<String> editAnnouncement(
            @PathVariable Long announcementId,
            @RequestBody AnnouncementDTO announcementDTO,
            Authentication authentication) {

        String email = authentication.getName();
        Users instructor = userService.findUserByEmail(email);

        Announcement announcement = announcementService.findAnnouncementById(announcementId);

        if (announcement == null) {
            return ResponseEntity.badRequest().body("Announcement not found");
        }

        Course course = announcement.getCourse();

        // Check if the instructor is assigned to the course of this announcement
        if (!course.getInstructor().getId().equals(instructor.getId())) {
            return ResponseEntity.status(403).body("You are not the instructor of this course");
        }

        announcement.setTitle(announcementDTO.getTitle());
        announcement.setContent(announcementDTO.getContent());
        announcementService.saveAnnouncement(announcement);

        return ResponseEntity.ok("Announcement updated successfully");
    }

    @PostMapping("/uploadSyllabus/{courseId}")
    public ResponseEntity<?> uploadSyllabus(@RequestParam("file") MultipartFile file, @PathVariable Long courseId, Authentication authentication) {

        if (!Objects.equals(file.getContentType(), "application/pdf")) {
            return ResponseEntity.badRequest().body("Only PDF Files are allowed.");
        }

        String email = authentication.getName();
        Users instructor = userService.findUserByEmail(email);

        Course course = courseService.findCourseById(courseId);

        if (course == null) {
            return ResponseEntity.badRequest().body("Invalid course ID");
        }

        // Check if the instructor is assigned to this course
        if (!course.getInstructor().getId().equals(instructor.getId())) {
            return ResponseEntity.status(403).body("You are not the instructor of this course");
        }

        return new ResponseEntity<>(fileService.uploadSyllabus(file,courseId), HttpStatus.OK);
    }


    @GetMapping("/{courseId}/syllabus")
    public ResponseEntity<?>  viewSyllabus(@PathVariable Long courseId) {

        // Find the course by ID
        Course course = courseService.findCourseById(courseId);

        if (course == null) {
            return ResponseEntity.badRequest().body("Invalid course ID");
        }

        // Check if a syllabus is uploaded
        String syllabusUrl = course.getSyllabusUrl();

        if (syllabusUrl == null || syllabusUrl.isEmpty()) {
            return ResponseEntity.badRequest().body("No syllabus uploaded for this course.");
        }

        final byte[] data = fileService.viewSyllabus(courseId);
        final ByteArrayResource resource = new ByteArrayResource(data);
        return ResponseEntity
                .ok()
                .contentLength(data.length)
                .header("Content-type", "application/octet-stream")
                .header("Content-disposition", "attachment; filename=\"" + syllabusUrl + "\"")
                .body(resource);

    }

    /* Assignment's endpoint */

    @PostMapping("/{courseId}/assignment/create")
    public ResponseEntity<String> createAssignment(@PathVariable Long courseId,
                                                       @RequestBody Assignment assignment,Authentication authentication) {

        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseId, authentication);

        if (authResponse != null) {
            return authResponse;
        }

        String email = authentication.getName();

        Users instructor = userService.findUserByEmail(email);

        Course course = courseService.findCourseById(courseId);

        if (assignment.getDeadline().before(new Date())) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("The deadline must be a future date.");
        }

        // Set course and creator (instructor)
        assignment.setCourse(course);
        assignment.setCreatedBy(instructor);

        assignmentService.createAssignment(assignment);

        return ResponseEntity.ok("Assignment Created successfully.");
    }

    // Update an assignment (only for instructors)
    @PutMapping("/{courseId}/assignment/{assignmentId}")
    public ResponseEntity<String> updateAssignment(@PathVariable Long courseId,
                                                       @PathVariable Long assignmentId,
                                                       @RequestBody Assignment updatedAssignment,
                                                   Authentication authentication) {

        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseId, authentication);

        if (authResponse != null) {
            return authResponse;
        }

        Course course = courseService.findCourseById(courseId);

        Assignment existingAssignment = assignmentService.getAssignmentById(assignmentId);

        if(existingAssignment == null) return ResponseEntity.badRequest().body("Invalid Assignnment ID");

        if(!existingAssignment.getCourse().getId().equals(course.getId())) {
            return ResponseEntity.status(403).body("This assignment doesn't belongs to the course");
        }

        existingAssignment.setTitle(updatedAssignment.getTitle());
        existingAssignment.setDescription(updatedAssignment.getDescription());
        existingAssignment.setDeadline(updatedAssignment.getDeadline());

        Assignment savedAssignment = assignmentService.updateAssignment(existingAssignment);
        return ResponseEntity.ok("Assignment updated successfully");
    }

    // Delete an assignment (only for instructors)
    @DeleteMapping("/{courseId}/assignment/{assignmentId}")
    public ResponseEntity<String> deleteAssignment(@PathVariable Long courseId,
                                                 @PathVariable Long assignmentId,
                                                 Authentication authentication) {

        ResponseEntity<String> authResponse = instructorCourseAuth.validateInstructorForCourse(courseId, authentication);

        if (authResponse != null) {
            return authResponse;
        }

        Course course = courseService.findCourseById(courseId);

        Assignment existingAssignment = assignmentService.getAssignmentById(assignmentId);

        if(existingAssignment == null) return ResponseEntity.badRequest().body("Invalid Assignnment ID");

        // Check if the assignment is assigned to this course
        if(!existingAssignment.getCourse().getId().equals(course.getId())) {
            return ResponseEntity.status(403).body("This assignment doesn't belongs to the course");
        }

        // delete the assignment
        assignmentService.deleteAssignment(assignmentId);

        return ResponseEntity.ok("Assignment Deleted successfully");
    }

    @GetMapping("/{courseID}/{assignmentId}/submissions")
    public ResponseEntity<?> getAssignmentSubmissions(@PathVariable Long courseID, @PathVariable Long assignmentId, Authentication authentication)
    {
        //Obtaining the email of the user from the authentication object
        String email = authentication.getName();
        //Obtaining the instructor details
        Users instructor = userService.findUserByEmail(email);

        Course course = courseService.findCourseById(courseID);

        if (course == null ) {
            return ResponseEntity.badRequest().body("Invalid course ID");
        }

        // Check if the instructor is assigned to this course, If not then an
        //appropriate error message is thrown.
        if (!course.getInstructor().getId().equals(instructor.getId()))
        {
            return ResponseEntity.status(403).body("You are not the instructor of this course");
        }

        //Get the Assignment details by passing the assignmentID
        Assignment assignmentDetails = assignmentService.getAssignmentById( assignmentId );
        //Check if the assignmentDetails is null
        if( assignmentDetails == null ){
            return ResponseEntity.badRequest().body("Assignment not found");
        }
        else
        {
            //Obtain the set of the submissions and send the details
            Set<Submission> assignmentSubmissions = assignmentDetails.getSubmissions();
            if( assignmentSubmissions.isEmpty() )
                return ResponseEntity.status(200).body("No submissions yet for this assignment");
            else
                return ResponseEntity.ok(assignmentSubmissions);

        }
    }

    @GetMapping("/{courseID}/submissions/{submissionID}")
    public ResponseEntity<?> getParticularAssignmentSubmission(@PathVariable Long courseID, @PathVariable Long submissionID, Authentication authentication){

        //Obtaining the email of the user from the authentication object
        String email = authentication.getName();
        //Obtaining the instructor details
        Users instructor = userService.findUserByEmail(email);

        Course course = courseService.findCourseById(courseID);

        if (course == null ) {
            return ResponseEntity.badRequest().body("Invalid course ID");
        }

        //Checking if the instructor is assigned to this course, If not then an
        //appropriate error message is thrown.
        if (!course.getInstructor().getId().equals(instructor.getId()))
        {
            return ResponseEntity.status(403).body("You are not the instructor of this course and hence not authorized to perform this operation");
        }

        //Get the submission details associated with the ID
        Submission uploadedSubmissionDetails = submissionService.getSubmissionByID( submissionID );
        if( uploadedSubmissionDetails == null )
            return ResponseEntity.badRequest().body("Submission not found");
        else
        {
            return new ResponseEntity<>( fileService.viewAssignmentSubmission( uploadedSubmissionDetails.getSubmissionUrl() ), HttpStatus.OK );
        }
    }

    @GetMapping("/{courseID}/submissions/{submissionID}/GradeBook")
    public ResponseEntity<?> getParticularSubmissionGradeBook(@PathVariable Long courseID, @PathVariable Long submissionID, Authentication authentication){
        //Obtaining the email of the user from the authentication object
        String email = authentication.getName();
        //Obtaining the instructor details
        Users instructor = userService.findUserByEmail(email);

        Course course = courseService.findCourseById(courseID);

        if (course == null ) {
            return ResponseEntity.badRequest().body("Invalid course ID");
        }

        //Checking if the instructor is assigned to this course, If not then an
        //appropriate error message is thrown.
        if (!course.getInstructor().getId().equals(instructor.getId()))
        {
            return ResponseEntity.status(403).body("You are not the instructor of this course and hence not authorized to perform this operation");
        }
        //Get the submission details associated with the ID
        Submission uploadedSubmissionDetails = submissionService.getSubmissionByID( submissionID );
        if( uploadedSubmissionDetails == null )
            return ResponseEntity.badRequest().body("Submission not found");
        else
        {
            Gradebook gradeBookDetails = uploadedSubmissionDetails.getGrade();
            if( gradeBookDetails == null )
                return ResponseEntity.status(200).body("No Grades/Feedback available for this submission yet");
            else
                return ResponseEntity.ok(gradeBookDetails);
        }

    }

    @PostMapping("/uploadCourseMaterial/{courseId}")
    public ResponseEntity<?> uploadCourseMaterial(@RequestParam("file") MultipartFile file, @PathVariable Long courseId,
                                                  @RequestParam("materialTitle") String courseMaterialTitle,
                                                  @RequestParam("materialDescription") String courseMaterialDescription,
                                                  Authentication authentication) {

        if (!Objects.equals(file.getContentType(), "application/pdf")) {
            return ResponseEntity.badRequest().body("Only PDF Files are allowed.");
        }

        String email = authentication.getName();

        Users instructor = userService.findUserByEmail(email);

        Course course = courseService.findCourseById(courseId);

        if (course == null) {
            return ResponseEntity.badRequest().body("Invalid course ID");
        }

        // Check if the instructor is assigned to this course
        if (!course.getInstructor().getId().equals(instructor.getId()))
        {
            return ResponseEntity.status(403).body("You are not the instructor of this course");
        }

        String instructorName = instructor.getFirstName()+"_"+instructor.getLastName()+"_"+instructor.getId();
        String courseTitle = course.getTitle()+"_"+course.getId();

        CourseMaterialDTO courseMaterialDetails= CourseMaterialDTO.builder()
                .materialTitle( courseMaterialTitle )
                .materialDescription( courseMaterialDescription )
                .build();
        //System.out.println( courseMaterialDetails );

        return new ResponseEntity<>(fileService.uploadCourseMaterial( file, instructorName, courseTitle, courseMaterialDetails), HttpStatus.OK);
    }

    @GetMapping("/getCourseMaterials/{courseId}")
    public ResponseEntity<?> getAllCourseMaterials(@PathVariable Long courseId, Authentication authentication)
    {
        String email = authentication.getName();
        Users employee = userService.findUserByEmail(email);

        Course course = courseService.findCourseById(courseId);

        if (course == null) {
            return ResponseEntity.badRequest().body("Invalid course ID");
        }

        //Check if the user is the actual instructor of the course by checking the ID of the instructor of the course
        //If the user is not the instructor, then it throws a 404 error
        if (!course.getInstructor().getId().equals(employee.getId())) {
            return ResponseEntity.status(403).body("You are not enrolled in this course as an instructor");
        }

        Set<CourseMaterial> courseMaterials = course.getCourseMaterials();

        if( courseMaterials.isEmpty() )
            return ResponseEntity.status(404).body("No Course Materials have been uploaded yet for this course");
        else
        {
            List<CourseMaterialDTO> courseMaterialDTOList = new ArrayList<>();
            courseMaterials.forEach(courseMaterial->{
                courseMaterialDTOList.add( CourseMaterialDTO.builder().
                        materialTitle( courseMaterial.getTitle() )
                        .materialDescription(courseMaterial.getDescription() ).build());

            });
            return ResponseEntity.ok(courseMaterialDTOList);
        }
    }

    @GetMapping("/getCourseMaterial/{courseId}/{materialTitle}")
    public ResponseEntity<?> getAllCourseMaterials(@PathVariable Long courseId, @PathVariable("materialTitle") String courseMaterialTitle, Authentication authentication)
    {
        String email = authentication.getName();
        Users employee = userService.findUserByEmail(email);

        Course course = courseService.findCourseById(courseId);

        if (course == null) {
            return ResponseEntity.badRequest().body("Invalid course ID");
        }

        //Check if the user is the actual instructor of the course by checking the ID of the instructor of the course
        //If the user is not the instructor, then it throws a 404 error
        if (!course.getInstructor().getId().equals(employee.getId())) {
            return ResponseEntity.status(403).body("You are not enrolled in this course as an instructor");
        }

        //Fetch the corresponding course material details
        CourseMaterial courseMaterial = courseMaterialService.getCourseMaterialByTitle( courseMaterialTitle.strip() );

        return new ResponseEntity<>(fileService.viewCourseMaterial( courseMaterial.getCourseMaterialUrl() ), HttpStatus.OK);

    }

    @PutMapping("/updateCourseMaterial/{courseId}/{currentMaterialTitle}")
    public ResponseEntity<?> updateCourseMaterial(@RequestParam("file") MultipartFile file, @PathVariable Long courseId,
                                                  @RequestParam("newMaterialTitle") String courseMaterialTitle,
                                                  @RequestParam("newMaterialDescription") String courseMaterialDescription,
                                                  @RequestParam("currentMaterialTitle") String existingCourseMaterialTitle,
                                                  Authentication authentication)
    {
        String email = authentication.getName();
        Users instructor = userService.findUserByEmail(email);

        Course course = courseService.findCourseById(courseId);

        if (course == null) {
            return ResponseEntity.badRequest().body("Invalid course ID");
        }

        //Check if the user is the actual instructor of the course by checking the ID of the instructor of the course
        //If the user is not the instructor, then it throws a 404 error
        if (!course.getInstructor().getId().equals(instructor.getId())) {
            return ResponseEntity.status(403).body("You are not authorized to perform this operation");
        }
        //Fetch the corresponding course material details
        CourseMaterial existingCourseMaterial = courseMaterialService.getCourseMaterialByTitle( existingCourseMaterialTitle.strip() );

        String instructorData = instructor.getFirstName()+"_"+instructor.getLastName()+"_"+instructor.getId();
        String courseData = course.getTitle()+"_"+course.getId();

        //Try deleting the existing file first before removing the file
        boolean isExistingCourseMaterialDeleted = fileService.deleteCourseMaterial( existingCourseMaterial.getCourseMaterialUrl() ).isDeletionSuccessfull();
        System.out.println( "Deletion Status of existing course material " + isExistingCourseMaterialDeleted );

        //If the existing course material has been deleted then we proceed to upload the new material.
        if( isExistingCourseMaterialDeleted )
        {
            CourseMaterialDTO newCourseMaterialDTO= CourseMaterialDTO.builder()
                    .materialTitle( courseMaterialTitle )
                    .materialDescription( courseMaterialDescription )
                    .build();
            return new ResponseEntity<>(fileService.updateCourseMaterial( file, instructorData, courseData, newCourseMaterialDTO, existingCourseMaterial ), HttpStatus.OK);

        }
        else
        {
            return ResponseEntity.status(200).body("Failed to delete the existing course material, Please try again later" );
        }
    }

    @DeleteMapping("/deleteCourseMaterial/{courseId}/{materialTitle}")
    public ResponseEntity<?> deleteCourseMaterial(@PathVariable Long courseId, @PathVariable("materialTitle") String courseMaterialTitle, Authentication authentication)
    {
        String email = authentication.getName();
        Users employee = userService.findUserByEmail(email);

        Course course = courseService.findCourseById(courseId);

        if (course == null) {
            return ResponseEntity.badRequest().body("Invalid course ID");
        }

        //Check if the user is the actual instructor of the course by checking the ID of the instructor of the course
        //If the user is not the instructor, then it throws a 404 error
        if (!course.getInstructor().getId().equals(employee.getId())) {
            return ResponseEntity.status(403).body("You are not authorized to perform this operation");
        }

        //Fetch the corresponding course material details
        CourseMaterial courseMaterial = courseMaterialService.getCourseMaterialByTitle( courseMaterialTitle.strip() );

        //Try deleting the existing file first before removing the file
        boolean isExistingCourseMaterialDeleted = fileService.deleteCourseMaterial( courseMaterial.getCourseMaterialUrl() ).isDeletionSuccessfull();

        if(  isExistingCourseMaterialDeleted )
        {
            return ResponseEntity.status(200).body("Course Material successfully removed from cloud storage");
        }
        else
        {
            return ResponseEntity.status(200).body("Failed to delete the existing course material, Please try again later" );
        }
    }

}
