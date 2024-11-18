package com.software.upskilled.Controller;

import com.software.upskilled.Entity.Assignment;
import com.software.upskilled.Entity.Course;
import com.software.upskilled.Entity.CourseMaterial;
import com.software.upskilled.Entity.Users;
import com.software.upskilled.dto.CourseDTO;
import com.software.upskilled.dto.CourseInfoDTO;
import com.software.upskilled.dto.CreateUserDTO;
import com.software.upskilled.service.*;
import com.software.upskilled.utils.AdminRoleAuth;
import com.software.upskilled.utils.ErrorResponseMessageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private FileService fileService;

    @Autowired
    private AssignmentService assignmentService;

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private CourseMaterialService courseMaterialService;

    @Autowired
    private AdminRoleAuth adminRoleAuth;

    @Autowired
    private ErrorResponseMessageUtil errorResponseMessageUtil;

    /**
     * Retrieves a combined list of active and inactive instructors.
     * Converts each instructor entity to a CreateUserDTO and masks the password field.
     * Returns the list of instructors as a response.
     *
     * @return ResponseEntity containing the list of instructors.
     */
    @GetMapping("/listInstructors")
    public ResponseEntity<List<CreateUserDTO>> getInstructorsList() {
        // Fetch and map inactive instructors to DTOs
        List<CreateUserDTO> instructorList = new ArrayList<>(userService.getInactiveInstructors().stream().map((instructor) -> {
            CreateUserDTO userDTO = new CreateUserDTO();
            userDTO.setEmail(instructor.getEmail());
            userDTO.setRole(instructor.getRole());
            userDTO.setPassword("*******");
            userDTO.setFirstName(instructor.getFirstName());
            userDTO.setLastName(instructor.getLastName());
            userDTO.setDesignation(instructor.getDesignation());
            userDTO.setId(instructor.getId());
            userDTO.setStatus(instructor.getStatus());
            return userDTO;
        }).toList());
        // Fetch and map active instructors to DTOs
        List<CreateUserDTO> activeInstructorList = userService.getActiveInstructors().stream().map((instructor) -> {
            CreateUserDTO userDTO = new CreateUserDTO();
            userDTO.setEmail(instructor.getEmail());
            userDTO.setRole(instructor.getRole());
            userDTO.setPassword("*******");
            userDTO.setFirstName(instructor.getFirstName());
            userDTO.setLastName(instructor.getLastName());
            userDTO.setDesignation(instructor.getDesignation());
            userDTO.setId(instructor.getId());
            userDTO.setStatus(instructor.getStatus());
            return userDTO;
        }).toList();
        // If no inactive instructors, return only active instructors
        if(instructorList.isEmpty()) {
            return ResponseEntity.ok(activeInstructorList);
        }
        // Combine inactive and active instructor lists
        instructorList.addAll(activeInstructorList);
        return ResponseEntity.ok(instructorList);
    }

    /**
     * Retrieves a list of active instructors.
     * Maps each instructor entity to a CreateUserDTO and masks the password field.
     *
     * @return ResponseEntity containing the list of active instructors.
     */
    @GetMapping("/listActiveInstructors")
    public ResponseEntity<List<CreateUserDTO>> getActiveInstructorsList() {
        List<CreateUserDTO> instructorList = new ArrayList<>(userService.getActiveInstructors().stream().map((instructor) -> {
            CreateUserDTO userDTO = new CreateUserDTO();
            userDTO.setEmail(instructor.getEmail());
            userDTO.setRole(instructor.getRole());
            userDTO.setPassword("*******");
            userDTO.setFirstName(instructor.getFirstName());
            userDTO.setLastName(instructor.getLastName());
            userDTO.setDesignation(instructor.getDesignation());
            userDTO.setId(instructor.getId());
            userDTO.setStatus(instructor.getStatus());
            return userDTO;
        }).toList());

        return ResponseEntity.ok(instructorList);
    }

    /**
     * Approves an instructor by changing their status to ACTIVE.
     *
     * @param instructorId The ID of the instructor to approve.
     * @return ResponseEntity with a success or error message.
     */
    @PostMapping("/approve/{instructorId}")
    public ResponseEntity<String> approveInstructor(@PathVariable Long instructorId) {
          Users instructor = userService.findUserById(instructorId);
        if (instructor == null || !instructor.getRole().equals("INSTRUCTOR")) {
            return ResponseEntity.badRequest().body("Invalid instructor ID");
        }
        instructor.setStatus(Users.Status.ACTIVE);
        userService.saveUser(instructor);
        return ResponseEntity.ok("User Approved!");
    }

    /**
     * Rejects an instructor by changing their status to REJECTED.
     *
     * @param instructorId The ID of the instructor to reject.
     * @return ResponseEntity with a success or error message.
     */
    @PostMapping("/reject/{instructorId}")
    public ResponseEntity<String> rejectInstructor(@PathVariable Long instructorId) {
        Users instructor = userService.findUserById(instructorId);
        if (instructor == null || !instructor.getRole().equals("INSTRUCTOR")) {
            return ResponseEntity.badRequest().body("Invalid instructor ID");
        }
        instructor.setStatus(Users.Status.REJECTED);
        userService.saveUser(instructor);
        return ResponseEntity.ok("User Rejected!");
    }

    /**
     * Creates a new course for an active instructor.
     * Validates the instructor's role, status, and course details before creation.
     *
     * @param courseDTO The course details provided in the request body.
     * @return ResponseEntity with a success or error message.
     */
    @PostMapping("/createCourse")
    public ResponseEntity<String> createNewCourse(@RequestBody CourseDTO courseDTO) {
        Users instructor = userService.findUserById(courseDTO.getInstructorId());
        if (instructor == null || (!instructor.getRole().equals("INSTRUCTOR") || !instructor.getStatus().toString().equalsIgnoreCase("ACTIVE"))) {
            return ResponseEntity.badRequest().body("Invalid instructor ID");
        }

        if(courseService.findByTitle(courseDTO.getTitle()) != null) {
            return ResponseEntity.badRequest().body("Course title already exists.");
        }

        if(courseDTO.getTitle().isBlank() || courseDTO.getDescription().isBlank() || courseDTO.getName().isBlank()) {
            return ResponseEntity.badRequest().body("Title, description or name missing!");
        }

        Course newCourse = Course.builder()
                .title(courseDTO.getTitle())
                .description(courseDTO.getDescription())
                .name(courseDTO.getName())
                .instructor(instructor)
                .status(Course.Status.ACTIVE).build();

        courseService.saveCourse(newCourse);

        return ResponseEntity.ok("Course created successfully for instructor: " + instructor.getEmail());
    }

    /**
     * Updates the details of an existing course.
     * Validates the course's existence, ensures unique titles, updates fields from the DTO,
     * and handles instructor reassignment with appropriate cleanup of associated resources.
     *
     * @param courseDTO The updated course details provided in the request body.
     * @param courseId The ID of the course to update.
     * @param user The authenticated user performing the update.
     * @return ResponseEntity with a success message or error details.
     */
    @PutMapping("/updateCourseDetails/{courseId}")
    public ResponseEntity<?> modifyCourseDetails(@RequestBody CourseDTO courseDTO, @PathVariable Long courseId, @AuthenticationPrincipal Users user) {

        Course course = courseService.findCourseById(courseId);

        if (course == null) {
            return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.BAD_REQUEST.value(), "Course doesn't exist with the particular courseID");
        }

        if(courseService.findByTitle(courseDTO.getTitle()) != null && !courseDTO.getTitle().equalsIgnoreCase(course.getTitle())) {
            return ResponseEntity.badRequest().body("Course title already exists.");
        }

        //Check for the values from the courseDTO
        if( !courseDTO.getTitle().isBlank() )
            //Get the newTitle from the DTO object and set it to the existing course
            course.setTitle( courseDTO.getTitle() );
        if( !courseDTO.getDescription().isBlank() )
            //Get the new Description from the DTO Object and set it to the existing course
            course.setDescription( courseDTO.getDescription() );
        if( !courseDTO.getName().isBlank() )
            //Get the new Name from the DTO Object and set it to existing Name
            course.setName( courseDTO.getName() );

        //Fetch the details of the new Instructor User
        Users instructor = userService.findUserById(courseDTO.getInstructorId());

        //If instructor ID is not null, then perform the change operation
        if (instructor != null && instructor.getStatus().equals(Users.Status.ACTIVE)) {
                //Save the instructor object to the exiting course details
            if(instructor.getId().equals(course.getInstructor().getId())) {
                course.setInstructor(instructor);
                courseService.saveCourse(course);
            }else {
//              Remove all the items related to previous instructor

                assignmentService.deleteAssignmentsByCourseId(course.getId());

                announcementService.deleteAnnouncementsByCourseId(course.getId());

                List<CourseMaterial> courseMaterials = courseMaterialService.getAllCourseMaterialsByCourseId(course.getId());

                for(CourseMaterial courseMaterial: courseMaterials) {
                    fileService.deleteCourseMaterial(courseMaterial.getCourseMaterialUrl());
                }

                courseMaterialService.deleteCourseMaterialsByCourseId(course.getId());

                course.setInstructor(instructor);
                courseService.saveCourse(course);
            }
        }else {
            return errorResponseMessageUtil.createErrorResponseMessages( HttpStatus.BAD_REQUEST.value(), "Instructor does not exists.");
        }
        return ResponseEntity.ok("Course Details updated successfully");
    }

    /**
     * Inactivates a course by its ID.
     * Marks the course status as INACTIVE if it exists.
     *
     * @param courseId The ID of the course to inactivate.
     * @return ResponseEntity with success or error message.
     */
    @PostMapping("/course/inactivate/{courseId}")
    public ResponseEntity<String> inactivateCourse(@PathVariable Long courseId) {
        Course course = courseService.findCourseById(courseId);

        if (course == null) {
            return ResponseEntity.badRequest().body("Course not found.");
        }

        course.setStatus(Course.Status.INACTIVE);
        courseService.saveCourse(course);
        return ResponseEntity.ok("Course Inactivated!");
    }

    /**
     * Retrieves a list of all active courses.
     * Maps each course to a DTO with course and instructor details.
     *
     * @return ResponseEntity containing the list of active courses.
     */
    @GetMapping("/courses")
    public ResponseEntity<List<CourseInfoDTO>> viewCourses() {

        List<CourseInfoDTO> courseList =  courseService.getAllCourses().stream()
                .filter(course -> course.getStatus().equals(Course.Status.ACTIVE))
                .map((course -> {
            CourseInfoDTO courseInfoDTO = new CourseInfoDTO();
            courseInfoDTO.setId(course.getId());
            courseInfoDTO.setTitle(course.getTitle());
            courseInfoDTO.setDescription(course.getDescription());
            courseInfoDTO.setInstructorId(course.getInstructor().getId());
            courseInfoDTO.setInstructorName(course.getInstructor().getFirstName() + " " + course.getInstructor().getLastName());
            courseInfoDTO.setName(course.getName());
            courseInfoDTO.setStatus(course.getStatus());
            return courseInfoDTO;
        })).collect(Collectors.toList());
        return ResponseEntity.ok(courseList);
    }

    /**
     * Retrieves detailed information about a specific course.
     * Returns course details if the course exists; otherwise, an error message.
     *
     * @param courseId The ID of the course to retrieve.
     * @return ResponseEntity containing the course details or an error message.
     */
    @GetMapping("/course/{courseId}")
    public ResponseEntity<?> getCourseDetails(@PathVariable Long courseId) {

        Course course = courseService.findCourseById(courseId);

        if (course == null) {
            return ResponseEntity.badRequest().body("Invalid course ID");
        }

        CourseInfoDTO courseInfoDTO = new CourseInfoDTO();
        courseInfoDTO.setId(course.getId());
        courseInfoDTO.setTitle(course.getTitle());
        courseInfoDTO.setDescription(course.getDescription());
        courseInfoDTO.setInstructorId(course.getInstructor().getId());
        courseInfoDTO.setInstructorName(course.getInstructor().getFirstName() + " " + course.getInstructor().getLastName());
        courseInfoDTO.setName(course.getName());
        courseInfoDTO.setStatus(course.getStatus());

        return ResponseEntity.ok(courseInfoDTO);
    }

    /**
     * Retrieves the syllabus file for a specific course.
     * Returns the syllabus file if uploaded; otherwise, an error message.
     *
     * @param courseId The ID of the course whose syllabus is requested.
     * @return ResponseEntity containing the syllabus file as a download or an error message.
     */
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
}
