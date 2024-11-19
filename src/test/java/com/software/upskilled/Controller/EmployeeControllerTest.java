package com.software.upskilled.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.software.upskilled.Entity.*;
import com.software.upskilled.dto.*;
import com.software.upskilled.service.*;
import com.software.upskilled.utils.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc( addFilters = false )
@ActiveProfiles("test")
public class EmployeeControllerTest
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
    private EmployeeCourseAuth employeeCourseAuth;

    @MockBean
    private CreateDTOObjectsImpl dtoObjectsCreator;

    @MockBean
    private CoursePropertyValidator coursePropertyValidator;

    @MockBean
    private AssignmentPropertyValidator assignmentPropertyValidator;

    @MockBean
    private EnrollmentService enrollmentService;

    @Test
    void testViewCourses() throws Exception {
        String email = "employee1@upskilled.com";

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        // Mock Employees (Receiver IDs)
        Users employee1 = new Users();
        employee1.setId(1L);
        employee1.setEmail("employee1@upskilled.com");

        Users employee2 = new Users();
        employee2.setId(2L);
        employee2.setEmail("employee2@upskilled.com");

        // Mock User (Instructor)
        Users instructor = new Users();
        instructor.setId(1L);
        instructor.setEmail(email);
        instructor.setFirstName("John");
        instructor.setLastName("Smith");
        instructor.setRole("INSTRUCTOR");

        Course course1 = new Course();
        course1.setId(101L);
        course1.setTitle("Course 1");
        course1.setName("Basic Course");
        course1.setDescription("Description 1");
        course1.setInstructor(instructor);
        course1.setStatus(Course.Status.ACTIVE);
        course1.setUpdatedAt( new Date());

        Course course2 = new Course();
        course2.setId(102L);
        course2.setTitle("Course 2");
        course2.setName("Basic Course 2");
        course2.setDescription("Description 2");
        course2.setInstructor(instructor);
        course2.setStatus(Course.Status.ACTIVE);
        course2.setUpdatedAt( new Date());

        // Mock Enrollments
        Enrollment enrollment1 = Enrollment.builder()
                .course(course1)
                .employee(employee1)
                .build();
        Enrollment enrollment2 = Enrollment.builder()
                .course(course1)
                .employee(employee2)
                .build();

        employee1.setEnrollments(Set.of(enrollment1));
        employee2.setEnrollments(Set.of(enrollment2));

        when(userService.findUserByEmail(email)).thenReturn(employee1);
        when(courseService.getAllCourses()).thenReturn(List.of(course1, course2));

        mockMvc.perform(get("/api/employee/courses")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(102L))
                .andExpect(jsonPath("$[0].title").value("Course 2"));

        //Verify that the invocations were made
        verify( userService ).findUserByEmail( email );
        verify(  courseService  ).getAllCourses();
    }


    @Test
    void testViewEnrolledCourses() throws Exception {
        String email = "employee1@upskilled.com";

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        // Mock Employees
        Users employee1 = new Users();
        employee1.setId(1L);
        employee1.setEmail("employee1@upskilled.com");


        // Mock User (Instructor)
        Users instructor = new Users();
        instructor.setId(1L);
        instructor.setEmail("instructor@upskilled.com");
        instructor.setFirstName("John");
        instructor.setLastName("Smith");
        instructor.setRole("INSTRUCTOR");

        // Mock Courses
        Course course1 = new Course();
        course1.setId(101L);
        course1.setTitle("Course 1");
        course1.setName("Basic Course");
        course1.setDescription("Description 1");
        course1.setInstructor(instructor);
        course1.setStatus(Course.Status.ACTIVE);
        course1.setUpdatedAt(new Date());

        Course course2 = new Course();
        course2.setId(102L);
        course2.setTitle("Course 2");
        course2.setName("Basic Course 2");
        course2.setDescription("Description 2");
        course2.setInstructor(instructor);
        course2.setStatus(Course.Status.ACTIVE);
        course2.setUpdatedAt(new Date(System.currentTimeMillis() + 86400000L));

        // Mock Enrollments
        Enrollment enrollment1 = Enrollment.builder()
                .course(course1)
                .employee(employee1)
                .build();
        Enrollment enrollment2 = Enrollment.builder()
                .course(course2)
                .employee(employee1)
                .build();

        employee1.setEnrollments(Set.of(enrollment1, enrollment2));

        when(userService.findUserByEmail(email)).thenReturn(employee1);

        mockMvc.perform(get("/api/employee/enrolledCourses")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(102L)) // Courses are sorted by update time
                .andExpect(jsonPath("$[0].title").value("Course 2"))
                .andExpect(jsonPath("$[1].id").value(101L))
                .andExpect(jsonPath("$[1].title").value("Course 1"));

        // Verify that the invocations were made
        verify(userService).findUserByEmail(email);
    }

    @Test
    void testGetCourseDetails() throws Exception {

        // Mock User (Instructor)
        Users instructor = new Users();
        instructor.setId(1L);
        instructor.setEmail("instructor@upskilled.com");
        instructor.setFirstName("John");
        instructor.setLastName("Smith");
        instructor.setRole("INSTRUCTOR");

        Course course = new Course();
        course.setId(101L);
        course.setTitle("ENPM-613");
        course.setDescription("Basic Description");
        course.setName("Design and Implementation");
        course.setStatus(Course.Status.ACTIVE);
        course.setInstructor( instructor );

        when(courseService.findCourseById(101L)).thenReturn(course);

        mockMvc.perform(get("/api/employee/course/101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(101L))
                .andExpect(jsonPath("$.title").value("ENPM-613"));

        verify(courseService).findCourseById(101L);
    }

    @Test
    void testCheckEnrollment() throws Exception {
        String email = "employee1@upskilled.com";

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        // Mock Employee
        Users employee1 = new Users();
        employee1.setId(1L);
        employee1.setEmail(email);

        // Mock Instructor
        Users instructor = new Users();
        instructor.setId(2L);
        instructor.setEmail("instructor@upskilled.com");
        instructor.setFirstName("John");
        instructor.setLastName("Smith");
        instructor.setRole("INSTRUCTOR");

        // Mock Course
        Course course1 = new Course();
        course1.setId(101L);
        course1.setTitle("Course 1");
        course1.setName("Basic Course");
        course1.setDescription("Description 1");
        course1.setInstructor(instructor);
        course1.setStatus(Course.Status.ACTIVE);

        // Mock Enrollment
        Enrollment enrollment = Enrollment.builder()
                .course(course1)
                .employee(employee1)
                .build();

        // Associate enrollment with employee
        employee1.setEnrollments(Set.of(enrollment));
        course1.setEnrollments( Set.of( enrollment ) );

        // Mock behavior
        when(userService.findUserByEmail(email)).thenReturn(employee1);
        when(courseService.findCourseById(101L)).thenReturn(course1);

        mockMvc.perform(get("/api/employee/enrollment/101")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string("Enrolled"));

        // Verify service interactions
        verify(userService).findUserByEmail(email);
        verify(courseService).findCourseById(101L);
    }

    @Test
    void testEnrollInCourse() throws Exception {
        String email = "employee@upskilled.com";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        Users employee = new Users();
        employee.setId(1L);
        employee.setEmail(email);
        when(userService.findUserByEmail(email)).thenReturn(employee);

        Course course = new Course();
        course.setId(101L);
        course.setStatus(Course.Status.ACTIVE);
        when(courseService.findCourseById(101L)).thenReturn(course);

        when(enrollmentService.enrollEmployee(101L, 1L)).thenReturn("Enrolled successfully!");

        mockMvc.perform(post("/api/employee/enroll")
                        .param("courseId", "101")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(content().string("Enrolled successfully!"));

        verify(userService).findUserByEmail(email);
        verify(courseService).findCourseById(101L);
        verify(enrollmentService).enrollEmployee(101L, 1L);
    }

    @Test
    void testViewAnnouncements() throws Exception {
        String email = "employee@upskilled.com";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        Users employee = new Users();
        employee.setId(1L);
        employee.setEmail(email);
        when(userService.findUserByEmail(email)).thenReturn(employee);

        Course course = new Course();
        course.setId(101L);
        course.setStatus(Course.Status.ACTIVE);
        when(courseService.findCourseById(101L)).thenReturn(course);

        Announcement announcement = new Announcement();
        announcement.setId(1L);
        announcement.setTitle("Announcement Title");
        announcement.setContent("Announcement Content");
        announcement.setUpdatedAt(new Date());

        when(announcementService.getAnnouncementsByCourseId(101L)).thenReturn(Set.of(announcement));

        mockMvc.perform(get("/api/employee/course/101/announcements")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Announcement Title"))
                .andExpect(jsonPath("$[0].content").value("Announcement Content"));

        verify(announcementService).getAnnouncementsByCourseId(101L);
    }

    @Test
    void testGetAnnouncementById() throws Exception {
        String email = "employee1@upskilled.com";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        // Mock Employee
        Users employee = new Users();
        employee.setId(1L);
        employee.setEmail(email);

        // Mock Instructor
        Users instructor = new Users();
        instructor.setId(2L);

        // Mock Course
        Course course = new Course();
        course.setId(101L);
        course.setInstructor(instructor);

        // Mock Announcement
        Announcement announcement = new Announcement();
        announcement.setId(1L);
        announcement.setTitle("Important Update");
        announcement.setContent("Please review the new guidelines.");
        announcement.setCourse(course);

        when(announcementService.findAnnouncementById(1L)).thenReturn(announcement);

        mockMvc.perform(get("/api/employee/getAnnouncementById/1")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Important Update"))
                .andExpect(jsonPath("$.content").value("Please review the new guidelines."));

        // Verify interactions
        verify(announcementService).findAnnouncementById(1L);
    }

    @Test
    void testViewSyllabus() throws Exception {
        // Mock Course
        Course course = new Course();
        course.setId(101L);
        course.setSyllabusUrl("syllabus.pdf");

        // Mock Syllabus Data
        byte[] syllabusData = "Sample PDF Content".getBytes();

        when(courseService.findCourseById(101L)).thenReturn(course);
        when(fileService.viewSyllabus(101L)).thenReturn(syllabusData);

        mockMvc.perform(get("/api/employee/101/syllabus"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-disposition", "attachment; filename=\"syllabus.pdf\""))
                .andExpect(content().bytes(syllabusData));

        // Verify interactions
        verify(courseService).findCourseById(101L);
        verify(fileService).viewSyllabus(101L);
    }

    @Test
    void testGetAllCourseMaterials() throws Exception {
        String email = "employee1@upskilled.com";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        // Mock Employee
        Users employee = new Users();
        employee.setId(1L);
        employee.setEmail(email);

        // Mock Course
        Course course = new Course();
        course.setId(101L);

        // Mock Course Materials
        CourseMaterial material1 = new CourseMaterial();
        material1.setId(1L);
        material1.setTitle("Material 1");
        material1.setDescription("Description 1");


        CourseMaterial material2 = new CourseMaterial();
        material2.setId(2L);
        material2.setTitle("Material 2");
        material2.setDescription("Description 2");

        course.setCourseMaterials(Set.of(material1, material2));
        when(courseService.findCourseById(101L)).thenReturn(course);

        mockMvc.perform(get("/api/employee/getCourseMaterials/101")
                        .principal(authentication))
                .andExpect(status().isOk());

        // Verify interactions
        verify(courseService).findCourseById(101L);
    }

    @Test
    void testGetCourseMaterialById() throws Exception {
        String email = "employee1@upskilled.com";
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        // Mock Employee
        Users employee = new Users();
        employee.setId(1L);
        employee.setEmail(email);

        // Mock Course Material
        CourseMaterial material = new CourseMaterial();
        material.setId(1L);
        material.setCourseMaterialUrl("Varad_Instructor_37/ENPM662/Discussion Week11 (1).pdf");

        // Mock File Data
        byte[] materialData = "Material PDF Content".getBytes();
        //ByteArrayResource materialDataResource = new ByteArrayResource( materialData );

        when(courseMaterialService.getCourseMaterialById(1L)).thenReturn(material);
        when(fileService.viewCourseMaterial( "Varad_Instructor_37/ENPM662/Discussion Week11 (1).pdf" )).thenReturn(materialData);

        mockMvc.perform(get("/api/employee/getCourseMaterial/101/1")
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-disposition", "attachment; filename=\"Discussion Week11 (1).pdf\""))
                .andExpect(content().bytes( materialData ));

        // Verify interactions
        verify(courseMaterialService).getCourseMaterialById(1L);
        verify(fileService).viewCourseMaterial("Varad_Instructor_37/ENPM662/Discussion Week11 (1).pdf");
    }

    @Test
    void testUploadAssignment() throws Exception {
        String email = "employee1@upskilled.com";
        String assignmentId = "1";
        String courseId = "101";

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        // Mock Employee
        Users employee = new Users();
        employee.setId(1L);
        employee.setEmail(email);

        // Mock Assignment
        Assignment assignment = new Assignment();
        assignment.setId(Long.parseLong(assignmentId));

        // Mock Course
        Course course = new Course();
        course.setId(Long.parseLong(courseId));
        assignment.setCourse(course);

        // Mock File
        MockMultipartFile file = new MockMultipartFile("file", "assignment.pdf", "application/pdf", "File Content".getBytes());

        // Mock Responses
        when(employeeCourseAuth.validateEmployeeForCourse(Long.parseLong(courseId), authentication)).thenReturn(null);
        when(userService.findUserByEmail(email)).thenReturn(employee);
        when(assignmentService.getAssignmentById(Long.parseLong(assignmentId))).thenReturn(assignment);
        when(fileService.uploadAssignmentSubmission(file, course, assignment, employee)).thenReturn( new FileUploadResponse());

        // Perform Request
        mockMvc.perform(multipart("/api/employee/uploadAssignment")
                        .file(file)
                        .param("assignmentId", assignmentId)
                        .param("courseId", courseId)
                        .principal(authentication))
                .andExpect(status().isOk());

        // Verify Interactions
        verify(employeeCourseAuth).validateEmployeeForCourse(Long.parseLong(courseId), authentication);
        verify(userService).findUserByEmail(email);
        verify(assignmentService).getAssignmentById(Long.parseLong(assignmentId));
        verify(fileService).uploadAssignmentSubmission(file, course, assignment, employee);
    }

    @Test
    void testGetAssignmentsForTheCourse() throws Exception {
        String email = "employee1@upskilled.com";
        Long courseId = 101L;

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        // Mock Employee
        Users employee = new Users();
        employee.setId(1L);
        employee.setEmail(email);

        // Mock Assignment
        Assignment assignment = new Assignment();
        assignment.setId(1L);
        assignment.setTitle("Test Assignment");
        assignment.setDescription("Assignment Description");
        assignment.setDeadline(1683571200000L); // some deadline timestamp

        // Mock Submission
        Submission submission = new Submission();
        submission.setId(1L);
        submission.setEmployee(employee);
        submission.setStatus(Submission.Status.SUBMITTED);
        submission.setSubmittedAt( new Date() );

        // Mocking Assignment's Submissions
        Set<Submission> submissions = new HashSet<>();
        submissions.add(submission);
        assignment.setSubmissions(submissions);

        // Mock Responses
        when(employeeCourseAuth.validateEmployeeForCourse(courseId, authentication)).thenReturn(null); // Authorized
        when(userService.findUserByEmail(email)).thenReturn(employee);
        when(assignmentService.getAllAssignmentsSortedByDeadLine(courseId)).thenReturn(Collections.singletonList(assignment));

        // Create SubmissionResponseDTO with mock values
        SubmissionResponseDTO submissionResponseDTO = new SubmissionResponseDTO();
        submissionResponseDTO.setSubmissionId(submission.getId());
        submissionResponseDTO.setSubmissionUrl("assignment.pdf");
        submissionResponseDTO.setSubmissionAt(submission.getSubmittedAt());
        submissionResponseDTO.setSubmissionStatus(submission.getStatus());
        submissionResponseDTO.setAssignmentID(assignment.getId());

        // Mocking DTO creation for Submission
        when(dtoObjectsCreator.createSubmissionDTO(eq(submission), eq(assignment), eq(employee)))
                .thenReturn(submissionResponseDTO);

        // Create AssignmentDetailsDTO and set its properties
        AssignmentDetailsDTO assignmentDetailsDTO = new AssignmentDetailsDTO();
        assignmentDetailsDTO.setTitle(assignment.getTitle());
        assignmentDetailsDTO.setDescription(assignment.getDescription());
        assignmentDetailsDTO.setDeadline(assignment.getDeadline());
        assignmentDetailsDTO.setId(assignment.getId());

        // Create AssignmentResponseDTO and set its properties
        AssignmentResponseDTO assignmentResponseDTO = new AssignmentResponseDTO();
        assignmentResponseDTO.setAssignmentDetails(assignmentDetailsDTO);
        assignmentResponseDTO.setSubmissionDetails(Collections.singletonList(submissionResponseDTO));

        // Perform Request
        mockMvc.perform(get("/api/employee/course/{courseId}/assignments", courseId)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))  // Verifying that one assignment is returned
                .andExpect(jsonPath("$[0].assignmentDetails.title").value("Test Assignment"))
                .andExpect(jsonPath("$[0].assignmentDetails.description").value("Assignment Description"))
                .andExpect(jsonPath("$[0].submissionDetails[0].submissionUrl").value("assignment.pdf"));

        // Verify Interactions
        verify(employeeCourseAuth).validateEmployeeForCourse(courseId, authentication);
        verify(userService).findUserByEmail(email);
        verify(assignmentService).getAllAssignmentsSortedByDeadLine(courseId);
        verify(dtoObjectsCreator).createSubmissionDTO(eq(submission), eq(assignment), eq(employee));
    }

    @Test
    public void testGetAssignmentByID() throws Exception {
        String email = "employee1@upskilled.com";
        Long courseId = 101L;
        Long assignmentId = 1L;

        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        // Mock Employee
        Users employee = new Users();
        employee.setId(1L);
        employee.setEmail(email);

        // Mock Assignment
        Assignment assignment = new Assignment();
        assignment.setId(assignmentId);
        assignment.setTitle("Test Assignment");
        assignment.setDescription("Assignment Description");
        assignment.setDeadline(1683571200000L); // some deadline timestamp

        // Mock Submission
        Submission submission = new Submission();
        submission.setId(1L);
        submission.setEmployee(employee);
        submission.setStatus(Submission.Status.SUBMITTED);
        submission.setSubmittedAt( new Date() );

        // Mocking Assignment's Submissions
        Set<Submission> submissions = new HashSet<>();
        submissions.add(submission);
        assignment.setSubmissions(submissions);

        // Mock Responses
        when(employeeCourseAuth.validateEmployeeForCourse(courseId, authentication)).thenReturn(null); // Authorized
        when(userService.findUserByEmail(email)).thenReturn(employee);
        when(assignmentService.getAssignmentById(assignmentId)).thenReturn(assignment);

        // Create SubmissionResponseDTO with mock values
        SubmissionResponseDTO submissionResponseDTO = new SubmissionResponseDTO();
        submissionResponseDTO.setSubmissionId(submission.getId());
        submissionResponseDTO.setSubmissionUrl("assignment.pdf");
        submissionResponseDTO.setSubmissionAt(submission.getSubmittedAt());
        submissionResponseDTO.setSubmissionStatus(submission.getStatus());
        submissionResponseDTO.setAssignmentID(assignment.getId());

        // Mocking DTO creation for Submission
        when(dtoObjectsCreator.createSubmissionDTO(eq(submission), eq(assignment), eq(employee)))
                .thenReturn(submissionResponseDTO);

        // Create AssignmentDetailsDTO and set its properties
        AssignmentDetailsDTO assignmentDetailsDTO = new AssignmentDetailsDTO();
        assignmentDetailsDTO.setTitle(assignment.getTitle());
        assignmentDetailsDTO.setDescription(assignment.getDescription());
        assignmentDetailsDTO.setDeadline(assignment.getDeadline());
        assignmentDetailsDTO.setId(assignment.getId());

        // Create AssignmentResponseDTO and set its properties
        AssignmentResponseDTO assignmentResponseDTO = new AssignmentResponseDTO();
        assignmentResponseDTO.setAssignmentDetails(assignmentDetailsDTO);
        assignmentResponseDTO.setSubmissionDetails(Collections.singletonList(submissionResponseDTO));

        when( dtoObjectsCreator.createAssignmentResponseDTO( eq(assignmentDetailsDTO) ,
                        eq(Collections.singletonList( submissionResponseDTO )) )).thenReturn( assignmentResponseDTO );

        // Perform Request
        mockMvc.perform(get("/api/employee/course/{courseId}/assignments/{assignmentId}", courseId, assignmentId)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignmentDetails.title").value("Test Assignment"))
                .andExpect(jsonPath("$.assignmentDetails.description").value("Assignment Description"))
                .andExpect(jsonPath("$.submissionDetails[0].submissionUrl").value("assignment.pdf"));

        // Verify Interactions
        verify(employeeCourseAuth).validateEmployeeForCourse(courseId, authentication);
        verify(userService).findUserByEmail(email);
        verify(assignmentService).getAssignmentById(assignmentId);
        verify(dtoObjectsCreator).createSubmissionDTO(eq(submission), eq(assignment), eq(employee));
        verify( dtoObjectsCreator ).createAssignmentResponseDTO( eq(assignmentDetailsDTO), eq( Collections.singletonList( submissionResponseDTO )) );
    }

    @Test
    void testSendMessageToInstructor() throws Exception {
        Long courseId = 1L;
        String email = "employee@upskilled.com";
        String messageContent = "Test message content";

        // Mock Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        // Mock Employee (Sender)
        Users employee = new Users();
        employee.setId(1L);
        employee.setEmail(email);
        employee.setFirstName("John");
        employee.setLastName("Smith");

        // Mock Course
        Course course = new Course();
        course.setId(courseId);
        course.setTitle("Course Title");

        // Mock Instructor (Recipient)
        Users instructor = new Users();
        instructor.setId(2L);
        instructor.setEmail("instructor@upskilled.com");
        instructor.setFirstName("Jane");
        instructor.setLastName("Doe");

        // Set the course instructor
        course.setInstructor(instructor);

        // Mock MessageRequestDTO
        MessageRequestDTO messageRequestDTO = new MessageRequestDTO();
        messageRequestDTO.setCourseId(courseId);
        messageRequestDTO.setMessage(messageContent);

        // Mock Services
        when(employeeCourseAuth.validateEmployeeForCourse(courseId, authentication)).thenReturn(null); // Authorized
        when(userService.findUserByEmail(email)).thenReturn(employee);
        when(courseService.findCourseById(courseId)).thenReturn(course);

        // Mock Message Service
        Message savedMessage = new Message();
        savedMessage.setId(101L);
        savedMessage.setSender(employee);
        savedMessage.setRecipient(instructor);
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

        mockMvc.perform(post("/api/employee/message/sendMessage")  // Correct endpoint path
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(messageRequestDTO))
                        .principal(authentication))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.messageId").value(savedMessage.getId()))
                .andExpect(jsonPath("$.message").value(messageContent))
                .andExpect(jsonPath("$.sentAt").exists())
                .andExpect(jsonPath("$.isRead").value(false));

        // Verify service interactions
        verify(employeeCourseAuth).validateEmployeeForCourse(courseId, authentication);
        verify(userService).findUserByEmail(email);
        verify(courseService).findCourseById(courseId);
        verify(messageService).createNewMessage(any(Message.class));
        verify(dtoObjectsCreator).createMessageResponseDTO(any(Message.class));
    }

    @Test
    void testGetMessagesSentToInstructor() throws Exception {
        Long courseId = 1L;
        String email = "employee@upskilled.com";

        // Mock Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        // Mock Employee (Sender)
        Users employee = new Users();
        employee.setId(1L);
        employee.setEmail(email);
        employee.setFirstName("John");
        employee.setLastName("Smith");

        // Mock Course
        Course course = new Course();
        course.setId(courseId);
        course.setTitle("Course Title");

        // Mock Instructor (Recipient)
        Users instructor = new Users();
        instructor.setId(2L);
        instructor.setEmail("instructor@upskilled.com");
        instructor.setFirstName("Jane");
        instructor.setLastName("Doe");

        // Set the course instructor
        course.setInstructor(instructor);

        // Mock Messages
        Message message1 = new Message();
        message1.setId(1L);
        message1.setSender(employee);
        message1.setRecipient(instructor);
        message1.setContent("Message from Employee to Instructor");
        message1.setCourse(course);
        message1.setIsRead(false);
        message1.setSentAt(new Date());

        // Mock MessageService to return sent messages
        Optional<List<Message>> sentMessages = Optional.of(Arrays.asList(message1));

        // Mock Services
        when(employeeCourseAuth.validateEmployeeForCourse(courseId, authentication)).thenReturn(null); // Authorized
        when(userService.findUserByEmail(email)).thenReturn(employee);
        when(messageService.getAllSentMessagesForEmployee(employee.getId(), courseId)).thenReturn(sentMessages);

        // Mock DTO creation
        Map<String, String> userDetails = new HashMap<>();
        userDetails.put("name", "John Smith");
        userDetails.put("email", email);

        List<MessageResponseDTO> messages = new ArrayList<>();
        MessageResponseDTO messageResponseDTO = new MessageResponseDTO();
        messageResponseDTO.setMessageId(message1.getId());
        messageResponseDTO.setMessage(message1.getContent());
        messageResponseDTO.setSentAt(message1.getSentAt());
        messageResponseDTO.setIsRead(message1.getIsRead());
        messages.add(messageResponseDTO);

        CourseMessagesResponseDTO courseMessagesResponseDTO = new CourseMessagesResponseDTO();
        courseMessagesResponseDTO.setUser(userDetails);
        courseMessagesResponseDTO.setMessages(messages);

        when(dtoObjectsCreator.createCourseMessagesResponseDTO(userDetails, Arrays.asList(message1))).thenReturn(courseMessagesResponseDTO);

        // Act and Assert
        ObjectMapper objectMapper = new ObjectMapper();

        mockMvc.perform(get("/api/employee/course/{courseId}/message/getSentMessages", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.user.name").value("John Smith"))
                .andExpect(jsonPath("$.messages[0].message").value("Message from Employee to Instructor"))
                .andExpect(jsonPath("$.messages[0].sentAt").exists())
                .andExpect(jsonPath("$.messages[0].isRead").value(false));

        // Verify service interactions
        verify(employeeCourseAuth).validateEmployeeForCourse(courseId, authentication);
        verify(userService).findUserByEmail(email);
        verify(messageService).getAllSentMessagesForEmployee(employee.getId(), courseId);
        verify(dtoObjectsCreator).createCourseMessagesResponseDTO(userDetails, Arrays.asList(message1));
    }

    @Test
    void testGetMessagesReceivedFromInstructor() throws Exception {
        Long courseId = 1L;
        String email = "john@upskilled.com";

        // Mock Authentication
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn(email);

        // Mock User (Employee)
        Users employee = new Users();
        employee.setId(1L);
        employee.setEmail(email);
        employee.setFirstName("John");
        employee.setLastName("Smith");
        employee.setRole("EMPLOYEE");

        // Mock Course
        Course course = new Course();
        course.setId(courseId);
        course.setTitle("Course Title");
        course.setInstructor(employee);

        // Mock Instructors (Senders)
        Long instructorId1 = 201L;
        Long instructorId2 = 202L;

        Users instructor1 = new Users();
        instructor1.setId(instructorId1);
        instructor1.setEmail("instructor1@upskilled.com");
        instructor1.setFirstName("Alice");
        instructor1.setLastName("Brown");

        Users instructor2 = new Users();
        instructor2.setId(instructorId2);
        instructor2.setEmail("instructor2@upskilled.com");
        instructor2.setFirstName("Bob");
        instructor2.setLastName("Green");

        // Mock Messages
        Message message1 = new Message();
        message1.setId(1L);
        message1.setSender(instructor1);
        message1.setRecipient(employee);
        message1.setContent("Message from Instructor 1");
        message1.setCourse(course);
        message1.setIsRead(false);
        message1.setSentAt(new Date());

        List<Message> singletonMessage = Arrays.asList(message1);

        Message message2 = new Message();
        message2.setId(2L);
        message2.setSender(instructor2);
        message2.setRecipient(employee);
        message2.setContent("Message from Instructor 2");
        message2.setCourse(course);
        message2.setIsRead(false);
        message2.setSentAt(new Date());

        List<Message> singletonMessageSecond = Arrays.asList(message2);

        // Mock MessageService
        when(employeeCourseAuth.validateEmployeeForCourse(courseId, authentication)).thenReturn(null);
        when(courseService.findCourseById(courseId)).thenReturn(course);
        when(userService.findUserByEmail("john@upskilled.com")).thenReturn( employee );
        when(messageService.getAllReceivedMessageForEmployee(employee.getId(), courseId)).thenReturn(Optional.of(Arrays.asList(message1, message2)));

        // Mock DTO creation
        Map<String, String> userDetails = new HashMap<>();
        userDetails.put("name", employee.getFirstName() + " " + employee.getLastName());
        userDetails.put("email", employee.getEmail());

        List<MessageResponseDTO> messages = new ArrayList<>();
        MessageResponseDTO messageResponse1 = new MessageResponseDTO();
        messageResponse1.setMessageId(message1.getId());
        messageResponse1.setMessage(message1.getContent());
        messageResponse1.setSentAt(message1.getSentAt());
        messageResponse1.setIsRead(message1.getIsRead());
        messages.add(messageResponse1);

        MessageResponseDTO messageResponse2 = new MessageResponseDTO();
        messageResponse2.setMessageId(message2.getId());
        messageResponse2.setMessage(message2.getContent());
        messageResponse2.setSentAt(message2.getSentAt());
        messageResponse2.setIsRead(message2.getIsRead());
        messages.add(messageResponse2);

        // Create Response DTO
        CourseMessagesResponseDTO courseReceivedMessagesResponseDTO = new CourseMessagesResponseDTO();
        courseReceivedMessagesResponseDTO.setUser(userDetails);
        courseReceivedMessagesResponseDTO.setMessages(messages);

        when(dtoObjectsCreator.createCourseMessagesResponseDTO(userDetails, Arrays.asList(message1, message2))).thenReturn(courseReceivedMessagesResponseDTO);
        when(dtoObjectsCreator.createMessageResponseDTO(message1)).thenReturn(messageResponse1);
        when(dtoObjectsCreator.createMessageResponseDTO(message2)).thenReturn(messageResponse2);

        // Act and Assert
        mockMvc.perform(get("/api/employee/course/{courseId}/message/getReceivedMessages", courseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .principal(authentication))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$").isNotEmpty())
                .andExpect(jsonPath("$.user.name").value("John Smith"))
                .andExpect(jsonPath("$.messages[0].message").value("Message from Instructor 1"))
                .andExpect(jsonPath("$.messages[1].message").value("Message from Instructor 2"));

        // Verify service interactions
        verify(employeeCourseAuth).validateEmployeeForCourse(courseId, authentication);
        verify(messageService).getAllReceivedMessageForEmployee(employee.getId(), courseId);
        verify(dtoObjectsCreator).createCourseMessagesResponseDTO(eq(userDetails), eq(Arrays.asList(message1,message2)));
    }

}
