package com.software.upskilled.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.software.upskilled.Entity.*;
import com.software.upskilled.dto.*;
import com.software.upskilled.service.*;
import com.software.upskilled.utils.AssignmentPropertyValidator;
import com.software.upskilled.utils.CoursePropertyValidator;
import com.software.upskilled.utils.CreateDTOObjectsImpl;
import com.software.upskilled.utils.InstructorCourseAuth;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc( addFilters = false )
@ActiveProfiles("test")
public class InstructorControllerTest
{
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private CourseService courseService;

    @MockBean
    private FileService fileService;

    @MockBean
    private AssignmentService assignmentService;

    @MockBean
    private AnnouncementService announcementService;

    @MockBean
    private SubmissionService submissionService;

    @MockBean
    private CourseMaterialService courseMaterialService;

    @MockBean
    private GradeBookService gradeBookService;

    @MockBean
    private MessageService messageService;

    @MockBean
    private InstructorCourseAuth instructorCourseAuth;

    @MockBean
    private CreateDTOObjectsImpl dtoObjectsCreator;

    @MockBean
    private CoursePropertyValidator coursePropertyValidator;

    @MockBean
    private AssignmentPropertyValidator assignmentPropertyValidator;


    @Test
    void testGetAllCoursesForInstructor() throws Exception {
        String email = "john@upskilled.com";

        // Mock Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);  // Simulate authenticated user

        // Mock userService to return a user when findUserByEmail is called
        Users activeInstructor = new Users();
        activeInstructor.setId(1L);
        activeInstructor.setEmail(email);
        activeInstructor.setStatus(Users.Status.ACTIVE);
        activeInstructor.setFirstName("John");
        activeInstructor.setLastName("Smith");
        activeInstructor.setRole("INSTRUCTOR");

        when(userService.findUserByEmail(email)).thenReturn( activeInstructor );

        // Mock courseService to return a list of courses
        Course course1 = new Course();
        course1.setId(101L);
        course1.setTitle("Course 1");
        course1.setDescription("Description 1");
        course1.setInstructor(activeInstructor);
        course1.setStatus(Course.Status.ACTIVE);
        course1.setUpdatedAt( new Date());

        Course course2 = new Course();
        course2.setId(102L);
        course2.setTitle("Course 2");
        course2.setDescription("Description 2");
        course2.setInstructor(activeInstructor);
        course2.setStatus(Course.Status.ACTIVE);
        course2.setUpdatedAt( new Date() );

        List<Course> courseList = Arrays.asList(course1, course2);
        when(courseService.findByInstructorId( 1L )).thenReturn(courseList);

        // Act and Assert
        mockMvc.perform(get("/api/instructor/courses")
                        .principal(authentication))  // Simulate authenticated user
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$[0].id").value(101L))  // Check that the most recently updated course is first
                .andExpect(jsonPath("$[0].title").value("Course 1"))
                .andExpect(jsonPath("$[1].id").value(102L))  // Check that the second course is in the list
                .andExpect(jsonPath("$[1].title").value("Course 2"))
                .andExpect(jsonPath("$[0].instructorName").value("John Smith"))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));

        // Verify interactions with mock services
        verify(userService).findUserByEmail(email);
        verify( courseService ).findByInstructorId( 1L );
    }

    @Test
    void testGetCourseDetails() throws Exception {
        Long courseId = 101L;
        String email = "john@upskilled.com";

        // Mock Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);  // Simulate authenticated user

        // Mock userService to return a user when findUserByEmail is called
        Users activeInstructor = new Users();
        activeInstructor.setId(1L);
        activeInstructor.setEmail(email);
        activeInstructor.setStatus(Users.Status.ACTIVE);
        activeInstructor.setFirstName("John");
        activeInstructor.setLastName("Smith");
        activeInstructor.setRole("INSTRUCTOR");

        when(userService.findUserByEmail(email)).thenReturn(activeInstructor);

        // Mock courseService to return a course by courseId
        Course course = new Course();
        course.setId(courseId);
        course.setTitle("Course 1");
        course.setName("Software Design");
        course.setDescription("Description 1");
        course.setInstructor(activeInstructor);
        course.setStatus(Course.Status.ACTIVE);

        when(courseService.findCourseById(courseId)).thenReturn(course);

        // Mocking the instructor authorization service to return a valid response
        ResponseEntity<String> authResponse = ResponseEntity.ok("Authorized");
        when(instructorCourseAuth.validateInstructorForCourse(courseId, authentication)).thenReturn(null);

        // Act and Assert
        mockMvc.perform(get("/api/instructor/course/{courseId}", courseId)
                        .principal(authentication))  // Simulate authenticated user
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.id").value(courseId))
                .andExpect(jsonPath("$.title").value("Course 1"))
                .andExpect(jsonPath("$.name").value("Software Design"))
                .andExpect(jsonPath("$.description").value("Description 1"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.instructorName").value("John Smith"));

        // Verify interactions with mock services
        verify(courseService).findCourseById(courseId);
        verify(instructorCourseAuth).validateInstructorForCourse(courseId, authentication);
    }

    @Test
    void testGetAnnouncementsForCourse() throws Exception {
        Long courseId = 101L;
        String email = "john@upskilled.com";

        // Mock Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);  // Simulate authenticated user

        // Mock userService to return a user when findUserByEmail is called
        Users activeInstructor = new Users();
        activeInstructor.setId(1L);
        activeInstructor.setEmail(email);
        activeInstructor.setStatus(Users.Status.ACTIVE);
        activeInstructor.setFirstName("John");
        activeInstructor.setLastName("Smith");
        activeInstructor.setRole("INSTRUCTOR");

        when(userService.findUserByEmail(email)).thenReturn(activeInstructor);

        // Mock instructorCourseAuth to allow access to the course
        // Mocking the instructor authorization service to return a valid response
        ResponseEntity<String> authResponse = ResponseEntity.ok("Authorized");
        when(instructorCourseAuth.validateInstructorForCourse(courseId, authentication)).thenReturn(null);

        // Mock announcementService to return a list of announcements
        Announcement announcement1 = new Announcement();
        announcement1.setId(1L);
        announcement1.setTitle("Announcement 1");
        announcement1.setContent("Content of Announcement 1");
        announcement1.setUpdatedAt(new Date());

        Announcement announcement2 = new Announcement();
        announcement2.setId(2L);
        announcement2.setTitle("Announcement 2");
        announcement2.setContent("Content of Announcement 2");
        announcement2.setUpdatedAt(new Date( System.currentTimeMillis() + 100000000L));

        Set<Announcement> announcements = new HashSet<>(Arrays.asList(announcement1, announcement2));
        when(announcementService.getAnnouncementsByCourseId(courseId)).thenReturn(announcements);

        // Act and Assert
        mockMvc.perform(get("/api/instructor/course/{courseId}/announcements", courseId)
                        .principal(authentication))  // Simulate authenticated user
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$[0].id").value(2L))  // Check the first announcement
                .andExpect(jsonPath("$[0].title").value("Announcement 2"))
                .andExpect(jsonPath("$[1].id").value(1L))  // Check the second announcement
                .andExpect(jsonPath("$[1].title").value("Announcement 1"))
                .andExpect(jsonPath("$[0].content").value("Content of Announcement 2"))
                .andExpect(jsonPath("$[1].content").value("Content of Announcement 1"));

        // Verify interactions with mock services
        verify(announcementService).getAnnouncementsByCourseId(courseId);
        verify(instructorCourseAuth).validateInstructorForCourse(courseId, authentication);
    }

    @Test
    void testCreateAnnouncement() throws Exception {
        Long courseId = 1L;
        String email = "john@upskilled.com";
        String title = "Important Update";
        String content = "Please note the new schedule for the course.";

        // Mock Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);  // Simulate authenticated user

        // Mock userService to return a user when findUserByEmail is called
        Users activeInstructor = new Users();
        activeInstructor.setId(1L);
        activeInstructor.setEmail(email);
        activeInstructor.setStatus(Users.Status.ACTIVE);
        activeInstructor.setFirstName("John");
        activeInstructor.setLastName("Smith");
        activeInstructor.setRole("INSTRUCTOR");

        when(userService.findUserByEmail(email)).thenReturn(activeInstructor);

        // Mock instructorCourseAuth to allow access to the course
        // Mocking the instructor authorization service to return a valid response
        when(instructorCourseAuth.validateInstructorForCourse(courseId, authentication)).thenReturn(null);

        // Create the AnnouncementDTO to be used in the request body
        AnnouncementDTO announcementDTO = new AnnouncementDTO();
        announcementDTO.setTitle(title);
        announcementDTO.setContent(content);

        // Mock announcementService to save the announcement
        Announcement savedAnnouncement = new Announcement();
        savedAnnouncement.setId(1L);
        savedAnnouncement.setTitle(title);
        savedAnnouncement.setContent(content);
        savedAnnouncement.setCourse(new Course());
        when(announcementService.saveAnnouncement(any(Announcement.class))).thenReturn(savedAnnouncement);

        // Act and Assert
        mockMvc.perform(post("/api/instructor/course/{courseId}/announcement", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"title\": \"" + title + "\", \"content\": \"" + content + "\" }")
                        .principal(authentication))  // Simulate authenticated user
                .andExpect(status().isOk())  // Check if the response is OK
                .andDo(print())  // Print response body for debugging
                .andExpect(content().string("Announcement created successfully"));  // Check the response body message

        // Verify interactions with mock services
        verify(announcementService).saveAnnouncement(any(Announcement.class));
        verify(instructorCourseAuth).validateInstructorForCourse(courseId, authentication);
    }

    @Test
    void testGetAnnouncementById() throws Exception {
        Long announcementId = 1L;
        String email = "john@upskilled.com";
        Long courseId = 101L;  // Course ID for the announcement

        // Mock Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);  // Simulate authenticated user

        // Mock userService to return a user when findUserByEmail is called
        Users activeInstructor = new Users();
        activeInstructor.setId(1L);
        activeInstructor.setEmail(email);
        activeInstructor.setStatus(Users.Status.ACTIVE);
        activeInstructor.setFirstName("John");
        activeInstructor.setLastName("Smith");
        activeInstructor.setRole("INSTRUCTOR");
        when(userService.findUserByEmail(email)).thenReturn(activeInstructor);

        // Mock AnnouncementService to return an announcement when findAnnouncementById is called
        Announcement announcement = new Announcement();
        announcement.setId(announcementId);
        announcement.setTitle("Announcement Title");
        announcement.setContent("Announcement Content");
        announcement.setUpdatedAt(new Date());
        Course course = new Course();
        course.setId(courseId);
        announcement.setCourse(course);

        when(announcementService.findAnnouncementById(announcementId)).thenReturn(announcement);

        // Mock InstructorCourseAuth to return null (i.e., no auth error)
        when(instructorCourseAuth.validateInstructorForCourse(courseId, authentication)).thenReturn(null);

        // Create the expected DTO
        AnnouncementRequestDTO expectedDTO = new AnnouncementRequestDTO();
        expectedDTO.setId(announcementId);
        expectedDTO.setTitle("Announcement Title");
        expectedDTO.setContent("Announcement Content");
        expectedDTO.setUpdatedAt(announcement.getUpdatedAt());

        // Perform the request and assert the result
        mockMvc.perform(get("/api/instructor/getAnnouncementById/{id}", announcementId)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(announcementId))
                .andExpect(jsonPath("$.title").value("Announcement Title"))
                .andExpect(jsonPath("$.content").value("Announcement Content"));

        // Verify the interactions with mock services
        verify(announcementService).findAnnouncementById(announcementId);
        verify(instructorCourseAuth).validateInstructorForCourse(courseId, authentication);
    }

    @Test
    void testUploadSyllabus_ValidFileUpload() throws Exception {
        Long courseId = 1L;
        String email = "john@upskilled.com";

        // Mock Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);  // Simulate authenticated user

        // Mock userService to return a user when findUserByEmail is called
        Users activeInstructor = new Users();
        activeInstructor.setId(1L);
        activeInstructor.setEmail(email);
        activeInstructor.setStatus(Users.Status.ACTIVE);
        activeInstructor.setFirstName("John");
        activeInstructor.setLastName("Smith");
        activeInstructor.setRole("INSTRUCTOR");

        when(userService.findUserByEmail(email)).thenReturn(activeInstructor);

        // Mock instructorCourseAuth to allow access to the course
        when(instructorCourseAuth.validateInstructorForCourse(courseId, authentication)).thenReturn(null);

        // Mock fileService to return a successful response when uploading the syllabus
        //Create the status of FileUpload Response
        FileUploadResponse fileUploadResponse = new FileUploadResponse();
        fileUploadResponse.setFilePath( "ENPM 613/ENPM613 Syllabus.pdf" );
        fileUploadResponse.setDateTime( LocalDateTime.now() );


        when(fileService.uploadSyllabus(any(MultipartFile.class), eq(courseId))).thenReturn( fileUploadResponse );

        // Create a valid mock MultipartFile (PDF)
        MockMultipartFile file = new MockMultipartFile("file", "syllabus.pdf", "application/pdf", "content".getBytes());

        // Act and Assert
        mockMvc.perform(multipart("/api/instructor/uploadSyllabus/{courseId}", courseId)
                        .file(file)
                        .principal(authentication))  // Simulate authenticated user
                .andExpect(status().isOk());  // Check success message

        // Verify interactions with mock services
        verify(fileService).uploadSyllabus(file, courseId);
        verify(instructorCourseAuth).validateInstructorForCourse(courseId, authentication);
    }

    @Test
    void testCreateAssignment() throws Exception {
        String email = "john@upskilled.com";
        Long courseId = 1L;

        // Mock Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);  // Simulate authenticated user

        // Mock User
        Users instructor = new Users();
        instructor.setId(1L);
        instructor.setEmail(email);
        instructor.setStatus(Users.Status.ACTIVE);
        instructor.setFirstName("John");
        instructor.setLastName("Smith");
        instructor.setRole("INSTRUCTOR");

        when(userService.findUserByEmail(email)).thenReturn(instructor);

        // Mock Course
        Course course = new Course();
        course.setId(courseId);
        course.setTitle("Software Design");
        course.setDescription("Description for Software Design");
        course.setInstructor(instructor);
        course.setStatus(Course.Status.ACTIVE);

        when(courseService.findCourseById(courseId)).thenReturn(course);

        // Mock Assignment
        Assignment assignment = new Assignment();
        assignment.setTitle("Assignment 1");
        assignment.setDescription("Description of Assignment 1");
        assignment.setDeadline(System.currentTimeMillis() + 86400000L);  // Deadline in the future (24 hours from now)

        // Mock Instructor Course Authorization
        when(instructorCourseAuth.validateInstructorForCourse(courseId, authentication)).thenReturn(null);

        // Act and Assert
        mockMvc.perform(post("/api/instructor/{courseId}/assignment/create", courseId)
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "title": "Assignment 1",
                            "description": "Description of Assignment 1",
                            "deadline": %s
                        }
                        """.formatted(assignment.getDeadline())))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string("Assignment Created successfully."));

        // Verify interactions with the mock services
        verify(instructorCourseAuth).validateInstructorForCourse(courseId, authentication);
        verify(userService).findUserByEmail(email);
        verify(courseService).findCourseById(courseId);
        verify(assignmentService).createAssignment(any(Assignment.class));
    }

    @Test
    void testGetAssignmentById() throws Exception {
        Long assignmentId = 1L;
        String email = "john@upskilled.com";

        // Mock Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        // Mock assignmentService to return an assignment
        Assignment assignment = new Assignment();
        assignment.setId(assignmentId);
        assignment.setTitle("Assignment Title");
        assignment.setDescription("Assignment Description");
        assignment.setDeadline( 1L );

        //Create Course Detail
        Course course = new Course();
        course.setId(101L);
        assignment.setCourse(course);
        when(assignmentService.getAssignmentById(assignmentId)).thenReturn(assignment);

        // Mock instructorCourseAuth to allow access
        when(instructorCourseAuth.validateInstructorForCourse(course.getId(), authentication)).thenReturn(null);

        // Mock dtoObjectsCreator to return a valid response DTO
        AssignmentDetailsDTO detailsDTO = new AssignmentDetailsDTO();
        detailsDTO.setId(assignment.getId());
        detailsDTO.setTitle(assignment.getTitle());
        detailsDTO.setDescription(assignment.getDescription());
        detailsDTO.setDeadline(assignment.getDeadline());

        AssignmentResponseDTO responseDTO = new AssignmentResponseDTO();
        responseDTO.setAssignmentDetails( detailsDTO );
        when(dtoObjectsCreator.createAssignmentResponseDTO(detailsDTO, null)).thenReturn(responseDTO);

        // Act and Assert
        mockMvc.perform(get("/api/instructor/getAssignmentById/{assignmentId}", assignmentId)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignmentDetails.id").value(assignmentId))
                .andExpect(jsonPath("$.assignmentDetails.title").value("Assignment Title"))
                .andExpect(jsonPath("$.assignmentDetails.description").value("Assignment Description"))
                .andExpect(jsonPath("$.assignmentDetails.deadline").isNotEmpty());

        // Verify interactions with mock services
        verify(assignmentService).getAssignmentById(assignmentId);
        verify(instructorCourseAuth).validateInstructorForCourse(course.getId(), authentication);
        verify(dtoObjectsCreator).createAssignmentResponseDTO(detailsDTO, null);
    }

    @Test
    void testUpdateAssignment() throws Exception {
        Long courseId = 1L;
        Long assignmentId = 101L;
        String email = "john@upskilled.com";

        // Mock Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        // Mock User
        Users instructor = new Users();
        instructor.setId(1L);
        instructor.setEmail(email);
        instructor.setFirstName("John");
        instructor.setLastName("Smith");
        instructor.setRole("INSTRUCTOR");

        // Mock Course
        Course course = new Course();
        course.setId(courseId);
        course.setTitle("Course Title");
        course.setInstructor(instructor);

        // Mock Existing Assignment
        Assignment existingAssignment = new Assignment();
        existingAssignment.setId(assignmentId);
        existingAssignment.setTitle("Old Title");
        existingAssignment.setDescription("Old Description");
        existingAssignment.setDeadline(System.currentTimeMillis() + 86400000L); // Future deadline
        existingAssignment.setCourse(course);

        // Mock Updated Assignment Payload
        Assignment updatedAssignment = new Assignment();
        updatedAssignment.setTitle("New Title");
        updatedAssignment.setDescription("New Description");
        updatedAssignment.setDeadline(System.currentTimeMillis() + 172800000L); // New future deadline

        // Mock Services
        when(instructorCourseAuth.validateInstructorForCourse(courseId, authentication)).thenReturn(null);
        when(courseService.findCourseById(courseId)).thenReturn(course);
        when(assignmentService.getAssignmentById(assignmentId)).thenReturn(existingAssignment);

        // Act and Assert
        mockMvc.perform(put("/api/instructor/{courseId}/assignment/{assignmentId}", courseId, assignmentId)
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "title": "New Title",
                            "description": "New Description",
                            "deadline": %s
                        }
                        """.formatted(updatedAssignment.getDeadline())))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(content().string("Assignment updated successfully"));

        // Verify interactions with mock services
        verify(instructorCourseAuth).validateInstructorForCourse(courseId, authentication);
        verify(courseService).findCourseById(courseId);
        verify(assignmentService).getAssignmentById(assignmentId);
        verify(assignmentService).updateAssignment(existingAssignment);

        // Ensure the existing assignment was updated
        assertEquals("New Title", existingAssignment.getTitle());
        assertEquals("New Description", existingAssignment.getDescription());
        assertEquals(updatedAssignment.getDeadline(), existingAssignment.getDeadline());
    }

    @Test
    void testGetAssignmentsForTheCourse() throws Exception {
        Long courseId = 1L;
        String email = "john@upskilled.com";

        // Mock Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        // Mock Course
        Course course = new Course();
        course.setId(courseId);
        course.setTitle("Course Title");

        // Mock Assignments
        Assignment assignment1 = new Assignment();
        assignment1.setId(101L);
        assignment1.setTitle("Assignment 1");
        assignment1.setDescription("Description 1");
        assignment1.setDeadline(System.currentTimeMillis() + 86400000L); // Future deadline

        Assignment assignment2 = new Assignment();
        assignment2.setId(102L);
        assignment2.setTitle("Assignment 2");
        assignment2.setDescription("Description 2");
        assignment2.setDeadline(System.currentTimeMillis() + 172800000L); // Another future deadline

        List<Assignment> assignments = Arrays.asList(assignment1, assignment2);

        // Mock DTOs
        AssignmentDetailsDTO assignmentDetailsDTO1 = new AssignmentDetailsDTO();
        assignmentDetailsDTO1.setId(101L);
        assignmentDetailsDTO1.setTitle("Assignment 1");
        assignmentDetailsDTO1.setDescription("Description 1");
        assignmentDetailsDTO1.setDeadline(assignment1.getDeadline());

        AssignmentDetailsDTO assignmentDetailsDTO2 = new AssignmentDetailsDTO();
        assignmentDetailsDTO2.setId(102L);
        assignmentDetailsDTO2.setTitle("Assignment 2");
        assignmentDetailsDTO2.setDescription("Description 2");
        assignmentDetailsDTO2.setDeadline(assignment2.getDeadline());

        AssignmentResponseDTO responseDTO1 = new AssignmentResponseDTO();
        responseDTO1.setAssignmentDetails(assignmentDetailsDTO1);

        AssignmentResponseDTO responseDTO2 = new AssignmentResponseDTO();
        responseDTO2.setAssignmentDetails(assignmentDetailsDTO2);

        // Mock Service and DTO Creator
        when(instructorCourseAuth.validateInstructorForCourse(courseId, authentication)).thenReturn(null);
        when(assignmentService.getAllAssignmentsSortedByDeadLine(courseId)).thenReturn(assignments);
        when(dtoObjectsCreator.createAssignmentResponseDTO(assignmentDetailsDTO1, null)).thenReturn(responseDTO1);
        when(dtoObjectsCreator.createAssignmentResponseDTO(assignmentDetailsDTO2, null)).thenReturn(responseDTO2);

        // Act and Assert
        mockMvc.perform(get("/api/instructor/course/{courseId}/assignments", courseId)
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$[0].assignmentDetails.id").value(101L))
                .andExpect(jsonPath("$[0].assignmentDetails.title").value("Assignment 1"))
                .andExpect(jsonPath("$[1].assignmentDetails.id").value(102L))
                .andExpect(jsonPath("$[1].assignmentDetails.title").value("Assignment 2"));

        // Verify interactions with mock services
        verify(instructorCourseAuth).validateInstructorForCourse(courseId, authentication);
        verify(assignmentService).getAllAssignmentsSortedByDeadLine(courseId);
        verify(dtoObjectsCreator).createAssignmentResponseDTO(assignmentDetailsDTO1, null);
        verify(dtoObjectsCreator).createAssignmentResponseDTO(assignmentDetailsDTO2, null);
    }

    @Test
    void testGetAssignmentSubmissions() throws Exception {
        Long courseId = 1L;
        Long assignmentId = 101L;
        String email = "john@upskilled.com";

        // Mock Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        // Mock Assignment
        Assignment assignment = new Assignment();
        assignment.setId(assignmentId);
        assignment.setTitle("Assignment Title");
        assignment.setDescription("Assignment Description");
        assignment.setDeadline(System.currentTimeMillis() + 86400000L); // Future deadline

        // Mock Submissions
        Submission submission1 = new Submission();
        submission1.setId(201L);
        submission1.setSubmittedAt(new Date());
        submission1.setEmployee( Users.builder()
                .id(1L)
                .email("johndoe@upskilled.com")
                .firstName("John")
                .lastName("Doe")
                .role("EMPLOYEE")
                .designation("Senior Project Manager")
                .status(Users.Status.ACTIVE)
                .build() );

        Submission submission2 = new Submission();
        submission2.setId(202L);
        submission2.setSubmittedAt(new Date());
        submission2.setEmployee( Users.builder()
                .id(2L)
                .email("janesmith@example.com")
                .firstName("Jane")
                .lastName("Smith")
                .role("EMPLOYEE")
                .designation("Senior Architect")
                .status(Users.Status.ACTIVE)
                .build() );

        List<Submission> submissions = Arrays.asList(submission1, submission2);

        // Mock DTOs
        AssignmentDetailsDTO assignmentDetailsDTO = new AssignmentDetailsDTO();
        assignmentDetailsDTO.setId(assignmentId);
        assignmentDetailsDTO.setTitle("Assignment Title");
        assignmentDetailsDTO.setDescription("Assignment Description");
        assignmentDetailsDTO.setDeadline(assignment.getDeadline());

        SubmissionResponseDTO submissionResponseDTO1 = new SubmissionResponseDTO();
        submissionResponseDTO1.setSubmissionId(201L);

        SubmissionResponseDTO submissionResponseDTO2 = new SubmissionResponseDTO();
        submissionResponseDTO2.setSubmissionId(202L);


        AssignmentResponseDTO assignmentResponseDTO = new AssignmentResponseDTO();
        assignmentResponseDTO.setAssignmentDetails(assignmentDetailsDTO);
        assignmentResponseDTO.setSubmissionDetails(Arrays.asList(submissionResponseDTO1, submissionResponseDTO2));

        // Mock Services and Validators
        when(instructorCourseAuth.validateInstructorForCourse(courseId, authentication)).thenReturn(null);
        when(coursePropertyValidator.isPropertyOfTheCourse(courseId, Map.of("assignment", assignmentId))).thenReturn(true);
        when(assignmentService.getAssignmentById(assignmentId)).thenReturn(assignment);
        when(submissionService.getSubmissionsSortedBySubmittedTime(assignmentId)).thenReturn(submissions);
        when(dtoObjectsCreator.createSubmissionDTO(submission1, assignment, submission1.getEmployee())).thenReturn(submissionResponseDTO1);
        when(dtoObjectsCreator.createSubmissionDTO(submission2, assignment, submission2.getEmployee())).thenReturn(submissionResponseDTO2);
        when(dtoObjectsCreator.createAssignmentResponseDTO(assignmentDetailsDTO, Arrays.asList(submissionResponseDTO1, submissionResponseDTO2))).thenReturn(assignmentResponseDTO);

        // Act and Assert
        mockMvc.perform(get("/api/instructor/{courseId}/{assignmentId}/submissions", courseId, assignmentId)
                        .principal(authentication)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.assignmentDetails.id").value(101L))
                .andExpect(jsonPath("$.assignmentDetails.title").value("Assignment Title"))
                .andExpect(jsonPath("$.submissionDetails[0].submissionId").value(201L))
                .andExpect(jsonPath("$.submissionDetails[1].submissionId").value(202L));

        // Verify interactions with mock services
        verify(instructorCourseAuth).validateInstructorForCourse(courseId, authentication);
        verify(coursePropertyValidator).isPropertyOfTheCourse(courseId, Map.of("assignment", assignmentId));
        verify(assignmentService).getAssignmentById(assignmentId);
        verify(submissionService).getSubmissionsSortedBySubmittedTime(assignmentId);
        verify(dtoObjectsCreator).createSubmissionDTO(submission1, assignment, submission1.getEmployee());
        verify(dtoObjectsCreator).createSubmissionDTO(submission2, assignment, submission2.getEmployee());
        verify(dtoObjectsCreator).createAssignmentResponseDTO(assignmentDetailsDTO, Arrays.asList(submissionResponseDTO1, submissionResponseDTO2));
    }

    @Test
    void testViewParticularAssignmentSubmission() throws Exception {
        Long courseId = 1L;
        Long assignmentId = 101L;
        Long submissionId = 201L;
        String email = "john@upskilled.com";
        String submissionFileUrl = "submission_201.pdf";
        byte[] fileData = "Sample PDF content".getBytes();

        // Mock Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        // Mock Submission
        Submission submission = new Submission();
        submission.setId(submissionId);
        submission.setSubmissionUrl(submissionFileUrl);

        // Mock Services
        when(instructorCourseAuth.validateInstructorForCourse(courseId, authentication)).thenReturn(null);
        when(submissionService.getSubmissionByID(submissionId)).thenReturn(submission);
        when(fileService.viewAssignmentSubmission(submissionFileUrl)).thenReturn(fileData);

        // Act and Assert
        mockMvc.perform(get("/api/instructor/{courseID}/assignments/{assignmentId}/submissions/{submissionID}/viewSubmission", courseId, assignmentId, submissionId)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(header().string("Content-disposition", "attachment; filename=\"" + submissionFileUrl + "\""))
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PDF_VALUE))
                .andExpect(content().bytes(fileData));

        // Verify interactions with mock services
        verify(instructorCourseAuth).validateInstructorForCourse(courseId, authentication);
        verify(submissionService).getSubmissionByID(submissionId);
        verify(fileService).viewAssignmentSubmission(submissionFileUrl);
    }

    @Test
    void testGetSubmissionDetailsForParticularSubmission() throws Exception {
        Long courseId = 1L;
        Long assignmentId = 101L;
        Long submissionId = 201L;
        String email = "john@upskilled.com";

        // Mock Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        // Mock Assignment
        Assignment assignment = new Assignment();
        assignment.setId(assignmentId);
        assignment.setTitle("Sample Assignment");
        assignment.setDescription("Sample Assignment Description");
        assignment.setDeadline(new Date().getTime());

        // Mock User
        Users employee = new Users();
        employee.setId(301L);
        employee.setFirstName("Jane");
        employee.setLastName("Doe");
        employee.setEmail("jane.doe@example.com");
        employee.setRole("EMPLOYEE");
        employee.setDesignation("Developer");
        employee.setStatus(Users.Status.ACTIVE);

        // Mock Submission
        Submission submission = new Submission();
        submission.setId(submissionId);
        submission.setSubmissionUrl("submission_201.pdf");
        submission.setSubmittedAt(new Date());
        submission.setStatus(Submission.Status.SUBMITTED);
        submission.setAssignment(assignment);
        submission.setEmployee( employee );

        // Mock CreateUserDTO
        CreateUserDTO userDetails = new CreateUserDTO();
        userDetails.setId(employee.getId());
        userDetails.setFirstName(employee.getFirstName());
        userDetails.setLastName(employee.getLastName());
        userDetails.setEmail(employee.getEmail());
        userDetails.setRole(employee.getRole());
        userDetails.setDesignation(employee.getDesignation());
        userDetails.setStatus(employee.getStatus());

        // Mock SubmissionResponseDTO
        SubmissionResponseDTO submissionResponseDTO = new SubmissionResponseDTO();
        submissionResponseDTO.setSubmissionId(submissionId);
        submissionResponseDTO.setSubmissionUrl(submission.getSubmissionUrl());
        submissionResponseDTO.setSubmissionAt(submission.getSubmittedAt());
        submissionResponseDTO.setSubmissionStatus(submission.getStatus());
        submissionResponseDTO.setAssignmentID(assignmentId);
        submissionResponseDTO.setUserDetails(userDetails);

        // Mock Service and Validator Behavior
        when(instructorCourseAuth.validateInstructorForCourse(courseId, authentication)).thenReturn(null);
        when(coursePropertyValidator.isPropertyOfTheCourse(courseId, Map.of("assignment", assignmentId))).thenReturn(true);
        when(assignmentService.getAssignmentById(assignmentId)).thenReturn(assignment);
        when(assignmentPropertyValidator.validateSubmissionAgainstAssignment(assignmentId, submissionId)).thenReturn(true);
        when(submissionService.getSubmissionByID(submissionId)).thenReturn(submission);
        when(dtoObjectsCreator.createSubmissionDTO(submission, assignment, employee)).thenReturn(submissionResponseDTO);

        // Act and Assert
        mockMvc.perform(get("/api/instructor/{courseID}/assignments/{assignmentId}/submissions/{submissionID}", courseId, assignmentId, submissionId)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.submissionId").value(submissionId))
                .andExpect(jsonPath("$.submissionUrl").value("submission_201.pdf"))
                .andExpect(jsonPath("$.submissionAt").isNotEmpty())
                .andExpect(jsonPath("$.submissionStatus").value(Submission.Status.SUBMITTED.toString()))
                .andExpect(jsonPath("$.assignmentID").value(assignmentId))
                .andExpect(jsonPath("$.userDetails.id").value(employee.getId()))
                .andExpect(jsonPath("$.userDetails.firstName").value("Jane"))
                .andExpect(jsonPath("$.userDetails.lastName").value("Doe"))
                .andExpect(jsonPath("$.userDetails.email").value("jane.doe@example.com"))
                .andExpect(jsonPath("$.userDetails.role").value("EMPLOYEE"))
                .andExpect(jsonPath("$.userDetails.designation").value("Developer"))
                .andExpect(jsonPath("$.userDetails.status").value(Users.Status.ACTIVE.toString()));

        // Verify interactions with mock services
        verify(instructorCourseAuth).validateInstructorForCourse(courseId, authentication);
        verify(coursePropertyValidator).isPropertyOfTheCourse(courseId, Map.of("assignment", assignmentId));
        verify(assignmentService).getAssignmentById(assignmentId);
        verify(assignmentPropertyValidator).validateSubmissionAgainstAssignment(assignmentId, submissionId);
        verify(submissionService).getSubmissionByID(submissionId);
        verify(dtoObjectsCreator).createSubmissionDTO(submission, assignment, employee);
    }

    @Test
    void testSubmitGradesToGradeBook() throws Exception {
        String courseID = "1";
        String submissionID = "101";
        String email = "instructor@upskilled.com";

        // Mock Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        // Mock Instructor
        Users instructor = new Users();
        instructor.setId(201L);
        instructor.setEmail(email);
        instructor.setFirstName("John");
        instructor.setLastName("Smith");
        instructor.setRole("INSTRUCTOR");
        instructor.setStatus(Users.Status.ACTIVE);

        // Mock Submission
        Submission submission = new Submission();
        submission.setId(Long.parseLong(submissionID));
        submission.setStatus(Submission.Status.SUBMITTED);

        // Mock GradeBookRequestDTO
        GradeBookRequestDTO gradingDetails = new GradeBookRequestDTO();
        gradingDetails.setGrade(85);
        gradingDetails.setFeedback("Well done!");

        // Mock Saved GradeBook
        Gradebook savedGradeBook = Gradebook.builder()
                .id(301L)
                .grade(gradingDetails.getGrade())
                .feedback(gradingDetails.getFeedback())
                .gradedAt(new Date())
                .submission(submission)
                .instructor(instructor)
                .build();

        // Updated Submission
        Submission updatedSubmission = new Submission();
        updatedSubmission.setId(Long.parseLong(submissionID));
        updatedSubmission.setStatus(Submission.Status.GRADED);
        updatedSubmission.setGrade( savedGradeBook );

        // Mock GradeBookResponseDTO
        GradeBookResponseDTO gradeBookResponseDTO = new GradeBookResponseDTO();
        gradeBookResponseDTO.setGrade(savedGradeBook.getGrade());
        gradeBookResponseDTO.setFeedback(savedGradeBook.getFeedback());
        gradeBookResponseDTO.setGradedDate(savedGradeBook.getGradedAt());
        gradeBookResponseDTO.setSubmissionID(savedGradeBook.getSubmission().getId());
        gradeBookResponseDTO.setInstructorID(savedGradeBook.getInstructor().getId());

        // Mock Service and Validator Behavior
        when(userService.findUserByEmail(email)).thenReturn(instructor);
        when(instructorCourseAuth.validateInstructorForCourse(Long.parseLong(courseID), authentication)).thenReturn(null);
        when(submissionService.getSubmissionByID(Long.parseLong(submissionID))).thenReturn(submission);
        when(gradeBookService.saveGradeBookSubmission(any(Gradebook.class))).thenReturn(savedGradeBook);
        when(submissionService.modifySubmissionDetails(any(Submission.class))).thenReturn( updatedSubmission );

        // Act and Assert
        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(post("/api/instructor/gradeBook/gradeAssignment")
                        .param("submissionId", submissionID)
                        .param("courseId", courseID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gradingDetails))
                        .principal(authentication))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.grade").value(gradingDetails.getGrade()))
                .andExpect(jsonPath("$.feedback").value(gradingDetails.getFeedback()))
                .andExpect(jsonPath("$.gradedDate").isNotEmpty())
                .andExpect(jsonPath("$.submissionID").value(Long.parseLong(submissionID)))
                .andExpect(jsonPath("$.instructorID").value(instructor.getId()));

        // Verify interactions with mock services
        verify(userService).findUserByEmail(email);
        verify(instructorCourseAuth).validateInstructorForCourse(Long.parseLong(courseID), authentication);
        verify(submissionService).getSubmissionByID(Long.parseLong(submissionID));
        verify(gradeBookService).saveGradeBookSubmission(any(Gradebook.class));
        verify(submissionService).modifySubmissionDetails(any(Submission.class));
    }

    @Test
    void testUpdateGradeDetails() throws Exception {
        String email = "instructor@upskilled.com";
        long gradingID = 301L;
        long courseID = 1L;
        long submissionID = 101L;

        // Mock Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        // Mock Instructor
        Users instructor = new Users();
        instructor.setId(201L);
        instructor.setEmail(email);
        instructor.setFirstName("John");
        instructor.setLastName("Smith");
        instructor.setRole("INSTRUCTOR");
        instructor.setStatus(Users.Status.ACTIVE);

        // Mock Submission
        Submission submission = new Submission();
        submission.setId(submissionID);

        // Mock Assignment and Course
        Assignment assignment = new Assignment();
        Course course = new Course();
        course.setId(courseID);
        assignment.setCourse(course);
        submission.setAssignment(assignment);

        // Mock GradeBook
        Gradebook existingGradeBook = Gradebook.builder()
                .grade(75)
                .feedback("Needs improvement")
                .gradedAt( new Date() )
                .submission(submission)
                .instructor(instructor)
                .id(gradingID)
                .build();

        // Mock Updated GradeBook
        GradeBookRequestDTO updatedGradeDetails = new GradeBookRequestDTO();
        updatedGradeDetails.setGrade(85);
        updatedGradeDetails.setFeedback("Good job!");

        Gradebook updatedGradeBook = Gradebook.builder()
                .grade(updatedGradeDetails.getGrade())
                .gradedAt( new Date() )
                .feedback(updatedGradeDetails.getFeedback())
                .submission(submission)
                .instructor(instructor)
                .id(gradingID)
                .build();

        // Mock GradeBookResponseDTO
        GradeBookResponseDTO gradeBookResponseDTO = new GradeBookResponseDTO();
        gradeBookResponseDTO.setGrade(updatedGradeBook.getGrade());
        gradeBookResponseDTO.setFeedback(updatedGradeBook.getFeedback());
        gradeBookResponseDTO.setGradedDate(updatedGradeBook.getGradedAt());
        gradeBookResponseDTO.setSubmissionID(updatedGradeBook.getSubmission().getId());
        gradeBookResponseDTO.setInstructorID(updatedGradeBook.getInstructor().getId());
        gradeBookResponseDTO.setGradeBookId(updatedGradeBook.getId());

        // Mock Service and Validator Behavior
        when(gradeBookService.getGradeBookByID(gradingID)).thenReturn(existingGradeBook);
        when(instructorCourseAuth.validateInstructorForCourse(courseID, authentication)).thenReturn(null);
        when(gradeBookService.saveGradeBookSubmission(any(Gradebook.class))).thenReturn(updatedGradeBook);
        when(dtoObjectsCreator.createGradeBookResponseDTO(updatedGradeBook, instructor.getId(), submission.getId()))
                .thenReturn(gradeBookResponseDTO);

        ObjectMapper objectMapper = new ObjectMapper();

        // Act and Assert
        mockMvc.perform(put("/api/instructor/gradeBook/updateGradeAssignment")
                        .principal( authentication )
                        .param("gradingId", String.valueOf(gradingID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedGradeDetails))
                        .principal(authentication))
                .andExpect(status().isOk())
                .andDo(print());

        // Verify interactions with mock services
        verify(gradeBookService).getGradeBookByID(gradingID);
        verify(instructorCourseAuth).validateInstructorForCourse(courseID, authentication);
        verify(gradeBookService).saveGradeBookSubmission(any(Gradebook.class));
        verify(dtoObjectsCreator).createGradeBookResponseDTO(existingGradeBook, instructor.getId(), submission.getId());
    }

    @Test
    void testUploadCourseMaterial() throws Exception {
        // Mock values
        String email = "instructor@upskilled.com";
        Long courseId = 1L;
        String courseMaterialTitle = "Course Material Title";
        String courseMaterialDescription = "Detailed course material description";

        // Mock file (PDF)
        MockMultipartFile file = new MockMultipartFile("file", "course-material.pdf", "application/pdf", "sample content".getBytes());

        // Mock Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        // Mock Instructor
        Users instructor = new Users();
        instructor.setId(201L);
        instructor.setEmail(email);
        instructor.setFirstName("John");
        instructor.setLastName("Smith");
        instructor.setRole("INSTRUCTOR");
        instructor.setStatus(Users.Status.ACTIVE);

        // Mock Course
        Course course = new Course();
        course.setId(courseId);
        course.setTitle("Course Title");

        // Mock CourseMaterialDTO
        CourseMaterialDTO courseMaterialDTO = new CourseMaterialDTO();
        courseMaterialDTO.setMaterialTitle(courseMaterialTitle);
        courseMaterialDTO.setMaterialDescription(courseMaterialDescription);

        // Mock Course Service and other dependencies
        when(userService.findUserByEmail(email)).thenReturn(instructor);
        when(courseService.findCourseById(courseId)).thenReturn(course);
        when(instructorCourseAuth.validateInstructorForCourse(courseId, authentication)).thenReturn(null);

        // Mock file service
        //Create the status of FileUpload Response
        FileUploadResponse fileUploadResponse = new FileUploadResponse();
        fileUploadResponse.setFilePath( "ENPM 613/ENPM613 Syllabus.pdf" );
        fileUploadResponse.setDateTime( LocalDateTime.now() );

        when(fileService.uploadCourseMaterial(file, "John_Smith_201", "Course Title_1", courseMaterialDTO))
                .thenReturn(fileUploadResponse);

        // Act and Assert
        mockMvc.perform(multipart("/api/instructor/uploadCourseMaterial/{courseId}", courseId)
                        .file(file)
                        .param("materialTitle", courseMaterialTitle)
                        .param("materialDescription", courseMaterialDescription)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andDo(print());

        // Verify interactions with mock services
        verify(userService).findUserByEmail(email);
        verify(courseService).findCourseById(courseId);
        verify(instructorCourseAuth).validateInstructorForCourse(courseId, authentication);
        verify(fileService).uploadCourseMaterial(refEq(file), refEq("John_Smith_201"), refEq("Course Title_1"), refEq( courseMaterialDTO));
    }

    @Test
    void testViewCourseMaterialById() throws Exception {
        Long courseId = 1L;
        Long courseMaterialId = 101L;
        String email = "john@upskilled.com";
        String courseMaterialUrl = "Varad_Instructor_37/ENPM662/Discussion Week11 (1).pdf";
        byte[] fileData = "Sample PDF content".getBytes();

        // Mock Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        // Mock CourseMaterial
        CourseMaterial courseMaterial = new CourseMaterial();
        courseMaterial.setId(courseMaterialId);
        courseMaterial.setCourseMaterialUrl(courseMaterialUrl);

        // Mock Services
        when(instructorCourseAuth.validateInstructorForCourse(courseId, authentication)).thenReturn(null);
        when(coursePropertyValidator.isPropertyOfTheCourse(courseId, Map.of("courseMaterial", courseMaterialId))).thenReturn(true);
        when(courseMaterialService.getCourseMaterialById(courseMaterialId)).thenReturn(courseMaterial);
        when(fileService.viewCourseMaterial(courseMaterialUrl)).thenReturn(fileData);

        // Act and Assert

        mockMvc.perform(get("/api/instructor/getCourseMaterial/{courseId}/{courseMaterialId}", courseId, courseMaterialId)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(header().string("Content-disposition", "attachment; filename=\"" + courseMaterialUrl.split("/")[2] + "\""))
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PDF_VALUE))
                .andExpect(content().bytes(fileData));
        // Verify interactions with mock services

        verify(instructorCourseAuth).validateInstructorForCourse(courseId, authentication);
        verify(coursePropertyValidator).isPropertyOfTheCourse(courseId, Map.of("courseMaterial", courseMaterialId));
        verify(courseMaterialService).getCourseMaterialById(courseMaterialId);
        verify(fileService).viewCourseMaterial(courseMaterialUrl);

    }

    @Test
    void testSendMessageToEmployees() throws Exception {
        Long courseId = 1L;
        Long employeeId1 = 101L;
        Long employeeId2 = 102L;
        String email = "john@upskilled.com";
        String messageContent = "Test message content";

        // Mock Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        // Mock User (Instructor)
        Users instructor = new Users();
        instructor.setId(1L);
        instructor.setEmail(email);
        instructor.setFirstName("John");
        instructor.setLastName("Smith");
        instructor.setRole("INSTRUCTOR");

        // Mock Course
        Course course = new Course();
        course.setId(courseId);
        course.setTitle("Course Title");
        course.setInstructor(instructor);

        // Mock Employees (Receiver IDs)
        Users employee1 = new Users();
        employee1.setId(employeeId1);
        employee1.setEmail("employee1@upskilled.com");

        Users employee2 = new Users();
        employee2.setId(employeeId2);
        employee2.setEmail("employee2@upskilled.com");

        // Mock Enrollments
        Enrollment enrollment1 = Enrollment.builder()
                .course(course)
                .employee(employee1)
                .build();
        Enrollment enrollment2 = Enrollment.builder()
                .course(course)
                .employee(employee2)
                .build();

        // Set of Enrollments
        Set<Enrollment> enrollments = new HashSet<>();
        enrollments.add(enrollment1);
        enrollments.add(enrollment2);
        course.setEnrollments( enrollments );
        //when(course.getEnrollments()).thenReturn(enrollments);

        // Mock MessageRequestDTO
        MessageRequestDTO messageRequestDTO = new MessageRequestDTO();
        messageRequestDTO.setCourseId(courseId);
        messageRequestDTO.setReceiverIds(Arrays.asList(employeeId1, employeeId2));
        messageRequestDTO.setMessage(messageContent);

        // Mock Services
        when(instructorCourseAuth.validateInstructorForCourse(courseId, authentication)).thenReturn(null);
        when(courseService.findCourseById(courseId)).thenReturn(course);
        when(userService.findUserById(employeeId1)).thenReturn(employee1);
        when(userService.findUserById(employeeId2)).thenReturn(employee2);

        // Mock Message Service
        Message savedMessage = new Message();
        savedMessage.setId(101L);
        savedMessage.setSender(instructor);
        savedMessage.setRecipient(employee1);
        savedMessage.setContent(messageContent);
        savedMessage.setCourse(course);
        savedMessage.setIsRead(false);
        savedMessage.setSentAt(new Date()); // Set sentAt to current date

        MessageResponseDTO messageResponseDTO = new MessageResponseDTO();
        messageResponseDTO.setMessageId(savedMessage.getId());
        messageResponseDTO.setMessage(messageContent);
        messageResponseDTO.setSentAt(savedMessage.getSentAt());
        messageResponseDTO.setIsRead(savedMessage.getIsRead());

        when(messageService.createNewMessage(any(Message.class))).thenReturn(savedMessage);
        when(dtoObjectsCreator.createMessageResponseDTO(any(Message.class))).thenReturn(messageResponseDTO);

        // Act and Assert
        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(post("/api/instructor/message/sendMessage")  // Updated to match the proper endpoint path
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageRequestDTO))
                        .principal(authentication))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].messageId").value(savedMessage.getId()))
                .andExpect(jsonPath("$[0].message").value(messageContent))
                .andExpect(jsonPath("$[0].sentAt").exists())
                .andExpect(jsonPath("$[0].isRead").value(false));

        // Verify service interactions
        verify(instructorCourseAuth).validateInstructorForCourse(courseId, authentication);
        verify(courseService).findCourseById(courseId);
        verify(userService).findUserById(employeeId1);
        verify(userService).findUserById(employeeId2);
        verify(messageService, times(2)).createNewMessage(any(Message.class));
        verify(dtoObjectsCreator, times(2)).createMessageResponseDTO(any(Message.class));
    }

    @Test
    void testGetMessagesSentToEmployee() throws Exception {
        Long courseId = 1L;
        String email = "john@upskilled.com";

        // Mock Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        // Mock User (Instructor)
        Users instructor = new Users();
        instructor.setId(1L);
        instructor.setEmail(email);
        instructor.setFirstName("John");
        instructor.setLastName("Smith");
        instructor.setRole("INSTRUCTOR");

        // Mock Course
        Course course = new Course();
        course.setId(courseId);
        course.setTitle("Course Title");
        course.setInstructor(instructor);

        // Mock Employees (Recipients)
        Long employeeId1 = 101L;
        Long employeeId2 = 102L;

        Users employee1 = new Users();
        employee1.setId(employeeId1);
        employee1.setEmail("employee1@upskilled.com");
        employee1.setFirstName("Harish");
        employee1.setLastName("Test");

        Users employee2 = new Users();
        employee2.setId(employeeId2);
        employee2.setEmail("employee2@upskilled.com");
        employee2.setFirstName("Satish");
        employee2.setLastName("Joe");

        // Mock Messages
        Message message1 = new Message();
        message1.setId(1L);
        message1.setSender(instructor);
        message1.setRecipient(employee1);
        message1.setContent("Message to Employee 1");
        message1.setCourse(course);
        message1.setIsRead(false);
        message1.setSentAt(new Date());

        List<Message> singletonMessage = Arrays.asList( message1 );

        Message message2 = new Message();
        message2.setId(2L);
        message2.setSender(instructor);
        message2.setRecipient(employee2);
        message2.setContent("Message to Employee 2");
        message2.setCourse(course);
        message2.setIsRead(false);
        message2.setSentAt(new Date());

        List<Message> singletonMessageSecond = Arrays.asList( message2 );



        // Mock MessageService
        when(instructorCourseAuth.validateInstructorForCourse(courseId, authentication)).thenReturn(null);
        when(courseService.findCourseById(courseId)).thenReturn(course);
        when(messageService.getUniqueListOfRecipientEmployeesForInstructor(instructor.getId(), courseId)).thenReturn(Arrays.asList(employeeId1, employeeId2));
        when( userService.findUserById(employeeId1) ).thenReturn( employee1 );
        when( userService.findUserById( employeeId2 )).thenReturn( employee2 );
        when(messageService.getAllReceivedMessageForEmployee(employeeId1, courseId)).thenReturn(Optional.of(Arrays.asList(message1)));
        when(messageService.getAllReceivedMessageForEmployee(employeeId2, courseId)).thenReturn(Optional.of(Arrays.asList(message2)));

        // Mock DTO creation
        Map<String, String> userDetails1 = new HashMap<>();
        userDetails1.put("name", "Harish Test");
        userDetails1.put("email", "employee1@upskilled.com");
        userDetails1.put("employeeId", "101");

        Map<String, String> userDetails2 = new HashMap<>();
        userDetails2.put("name", "Satish Joe");
        userDetails2.put("email", "employee2@upskilled.com");
        userDetails2.put("employeeId", "102");

        List<MessageResponseDTO> messages1 = new ArrayList<>();
        MessageResponseDTO messageResponse1 = new MessageResponseDTO();
        messageResponse1.setMessageId(message1.getId());
        messageResponse1.setMessage(message1.getContent());
        messageResponse1.setSentAt(message1.getSentAt());
        messageResponse1.setIsRead(message1.getIsRead());
        messages1.add(messageResponse1);

        List<MessageResponseDTO> messages2 = new ArrayList<>();
        MessageResponseDTO messageResponse2 = new MessageResponseDTO();
        messageResponse2.setMessageId(message2.getId());
        messageResponse2.setMessage(message2.getContent());
        messageResponse2.setSentAt(message2.getSentAt());
        messageResponse2.setIsRead(message2.getIsRead());
        messages2.add(messageResponse2);


        List<CourseMessagesResponseDTO> courseMessagesResponseDTOList = new ArrayList<>();

        CourseMessagesResponseDTO courseMessagesFirstResponseDTO = new CourseMessagesResponseDTO();
        courseMessagesFirstResponseDTO.setUser( userDetails1 );
        courseMessagesFirstResponseDTO.setMessages( messages1 );

        when( dtoObjectsCreator.createCourseMessagesResponseDTO( userDetails1, singletonMessage ) ).thenReturn( courseMessagesFirstResponseDTO );

        CourseMessagesResponseDTO courseMessagesSecondResponseDTO = new CourseMessagesResponseDTO();
        courseMessagesSecondResponseDTO.setUser( userDetails2 );
        courseMessagesSecondResponseDTO.setMessages( messages2 );

        when( dtoObjectsCreator.createCourseMessagesResponseDTO( userDetails2, singletonMessageSecond ) ).thenReturn( courseMessagesSecondResponseDTO );

        courseMessagesResponseDTOList.add( courseMessagesFirstResponseDTO );
        courseMessagesResponseDTOList.add( courseMessagesSecondResponseDTO );


        // Act and Assert
        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(get("/api/instructor/course/{courseId}/message/getSentMessages", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].user.name").value("Harish Test"))
                .andExpect(jsonPath("$[0].messages[0].message").value("Message to Employee 1"))
                .andExpect(jsonPath("$[1].user.name").value("Satish Joe"))
                .andExpect(jsonPath("$[1].messages[0].message").value("Message to Employee 2"));

        // Verify service interactions
        verify(instructorCourseAuth).validateInstructorForCourse(courseId, authentication);
        verify(courseService).findCourseById(courseId);
        verify(messageService).getUniqueListOfRecipientEmployeesForInstructor(instructor.getId(), courseId);
        verify(messageService).getAllReceivedMessageForEmployee(employeeId1, courseId);
        verify(messageService).getAllReceivedMessageForEmployee(employeeId2, courseId);
    }

    @Test
    void testGetMessagesReceivedFromEmployee() throws Exception {
        Long courseId = 1L;
        String email = "john@upskilled.com";

        // Mock Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        // Mock User (Instructor)
        Users instructor = new Users();
        instructor.setId(1L);
        instructor.setEmail(email);
        instructor.setFirstName("John");
        instructor.setLastName("Smith");
        instructor.setRole("INSTRUCTOR");

        // Mock Course
        Course course = new Course();
        course.setId(courseId);
        course.setTitle("Course Title");
        course.setInstructor(instructor);

        // Mock Employees (Senders)
        Long employeeId1 = 101L;
        Long employeeId2 = 102L;

        Users employee1 = new Users();
        employee1.setId(employeeId1);
        employee1.setEmail("employee1@upskilled.com");
        employee1.setFirstName("Harish");
        employee1.setLastName("Test");

        Users employee2 = new Users();
        employee2.setId(employeeId2);
        employee2.setEmail("employee2@upskilled.com");
        employee2.setFirstName("Satish");
        employee2.setLastName("Joe");

        // Mock Messages
        Message message1 = new Message();
        message1.setId(1L);
        message1.setSender(employee1);
        message1.setRecipient(instructor);
        message1.setContent("Message from Employee 1");
        message1.setCourse(course);
        message1.setIsRead(false);
        message1.setSentAt(new Date());

        List<Message> singletonMessage = Arrays.asList(message1);

        Message message2 = new Message();
        message2.setId(2L);
        message2.setSender(employee2);
        message2.setRecipient(instructor);
        message2.setContent("Message from Employee 2");
        message2.setCourse(course);
        message2.setIsRead(false);
        message2.setSentAt(new Date());

        List<Message> singletonMessageSecond = Arrays.asList(message2);

        // Mock MessageService
        when(instructorCourseAuth.validateInstructorForCourse(courseId, authentication)).thenReturn(null);
        when(courseService.findCourseById(courseId)).thenReturn(course);
        when(messageService.getUniqueListOfSenderEmployeesForInstructor(instructor.getId(), courseId))
                .thenReturn(Arrays.asList(employeeId1, employeeId2));
        when(userService.findUserById(employeeId1)).thenReturn(employee1);
        when(userService.findUserById(employeeId2)).thenReturn(employee2);
        when(messageService.getAllSentMessagesForEmployee(employeeId1, courseId)).thenReturn(Optional.of(Arrays.asList(message1)));
        when(messageService.getAllSentMessagesForEmployee(employeeId2, courseId)).thenReturn(Optional.of(Arrays.asList(message2)));

        // Mock DTO creation
        Map<String, String> userDetails1 = new HashMap<>();
        userDetails1.put("name", "Harish Test");
        userDetails1.put("email", "employee1@upskilled.com");
        userDetails1.put("employeeId", "101");

        Map<String, String> userDetails2 = new HashMap<>();
        userDetails2.put("name", "Satish Joe");
        userDetails2.put("email", "employee2@upskilled.com");
        userDetails2.put("employeeId", "102");

        List<MessageResponseDTO> messages1 = new ArrayList<>();
        MessageResponseDTO messageResponse1 = new MessageResponseDTO();
        messageResponse1.setMessageId(message1.getId());
        messageResponse1.setMessage(message1.getContent());
        messageResponse1.setSentAt(message1.getSentAt());
        messageResponse1.setIsRead(message1.getIsRead());
        messages1.add(messageResponse1);

        List<MessageResponseDTO> messages2 = new ArrayList<>();
        MessageResponseDTO messageResponse2 = new MessageResponseDTO();
        messageResponse2.setMessageId(message2.getId());
        messageResponse2.setMessage(message2.getContent());
        messageResponse2.setSentAt(message2.getSentAt());
        messageResponse2.setIsRead(message2.getIsRead());
        messages2.add(messageResponse2);

        List<CourseMessagesResponseDTO> courseMessagesResponseDTOList = new ArrayList<>();

        CourseMessagesResponseDTO courseMessagesFirstResponseDTO = new CourseMessagesResponseDTO();
        courseMessagesFirstResponseDTO.setUser(userDetails1);
        courseMessagesFirstResponseDTO.setMessages(messages1);

        when(dtoObjectsCreator.createCourseMessagesResponseDTO(userDetails1, singletonMessage)).thenReturn(courseMessagesFirstResponseDTO);

        CourseMessagesResponseDTO courseMessagesSecondResponseDTO = new CourseMessagesResponseDTO();
        courseMessagesSecondResponseDTO.setUser(userDetails2);
        courseMessagesSecondResponseDTO.setMessages(messages2);

        when(dtoObjectsCreator.createCourseMessagesResponseDTO(userDetails2, singletonMessageSecond)).thenReturn(courseMessagesSecondResponseDTO);

        courseMessagesResponseDTOList.add(courseMessagesFirstResponseDTO);
        courseMessagesResponseDTOList.add(courseMessagesSecondResponseDTO);

        // Act and Assert
        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(get("/api/instructor/course/{courseId}/message/getReceivedMessages", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].user.name").value("Harish Test"))
                .andExpect(jsonPath("$[0].messages[0].message").value("Message from Employee 1"))
                .andExpect(jsonPath("$[1].user.name").value("Satish Joe"))
                .andExpect(jsonPath("$[1].messages[0].message").value("Message from Employee 2"));

        // Verify service interactions
        verify(instructorCourseAuth).validateInstructorForCourse(courseId, authentication);
        verify(courseService).findCourseById(courseId);
        verify(messageService).getUniqueListOfSenderEmployeesForInstructor(instructor.getId(), courseId);
        verify(messageService).getAllSentMessagesForEmployee(employeeId1, courseId);
        verify(messageService).getAllSentMessagesForEmployee(employeeId2, courseId);
    }

}
