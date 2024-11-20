package com.software.upskilled.service;

import com.software.upskilled.Entity.Course;
import com.software.upskilled.Entity.Message;
import com.software.upskilled.Entity.Users;
import com.software.upskilled.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
public class MessageServiceTest
{
    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private MessageService messageService;

    private Message message;
    private Users sender, recipient;
    private Course course;

    @BeforeEach
    void setUp() {
        // Mock Users
        sender = new Users();
        sender.setId(1L);
        sender.setEmail("sender@test.com");

        recipient = new Users();
        recipient.setId(2L);
        recipient.setEmail("recipient@test.com");

        // Mock Course
        course = new Course();
        course.setId(101L);
        course.setTitle("Course 101");

        // Mock Message
        message = new Message();
        message.setId(1L);
        message.setContent("This is a test message");
        message.setSender(sender);
        message.setRecipient(recipient);
        message.setCourse(course);
        message.setIsRead(false);
    }

    @Test
    void testCreateNewMessage() {
        // Arrange
        when(messageRepository.save(any(Message.class))).thenReturn(message);

        // Act
        Message savedMessage = messageService.createNewMessage(message);

        // Assert
        assertNotNull(savedMessage);
        assertEquals(message.getId(), savedMessage.getId());
        assertEquals(message.getContent(), savedMessage.getContent());
        verify(messageRepository, times(1)).save(message);
    }

    @Test
    void testGetAllSentMessagesForEmployee() {
        // Arrange
        List<Message> sentMessages = List.of(message);
        when(messageRepository.getSentMessagesForEmployee(1L, 101L)).thenReturn(Optional.of(sentMessages));

        // Act
        Optional<List<Message>> result = messageService.getAllSentMessagesForEmployee(1L, 101L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        assertEquals(message.getContent(), result.get().get(0).getContent());
        verify(messageRepository, times(1)).getSentMessagesForEmployee(1L, 101L);
    }

    @Test
    void testGetAllReceivedMessagesForEmployee() {
        // Arrange
        List<Message> receivedMessages = List.of(message);
        when(messageRepository.getReceivedMessagesForEmployee(2L, 101L)).thenReturn(Optional.of(receivedMessages));

        // Act
        Optional<List<Message>> result = messageService.getAllReceivedMessageForEmployee(2L, 101L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1, result.get().size());
        assertEquals(message.getContent(), result.get().get(0).getContent());
        verify(messageRepository, times(1)).getReceivedMessagesForEmployee(2L, 101L);
    }

    @Test
    void testGetUniqueListOfSenderEmployeesForInstructor() {
        // Arrange
        List<Long> senderIds = List.of(1L, 2L);
        when(messageRepository.getUniqueEmployeeSenderListForInstructor(3L, 101L)).thenReturn(senderIds);

        // Act
        List<Long> result = messageService.getUniqueListOfSenderEmployeesForInstructor(3L, 101L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(messageRepository, times(1)).getUniqueEmployeeSenderListForInstructor(3L, 101L);
    }

    @Test
    void testGetUniqueListOfRecipientEmployeesForInstructor() {
        // Arrange
        List<Long> recipientIds = List.of(2L, 3L);
        when(messageRepository.getUniqueEmployeeRecipientListFromInstructor(1L, 101L)).thenReturn(recipientIds);

        // Act
        List<Long> result = messageService.getUniqueListOfRecipientEmployeesForInstructor(1L, 101L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(messageRepository, times(1)).getUniqueEmployeeRecipientListFromInstructor(1L, 101L);
    }

    @Test
    void testGetMessageById() {
        // Arrange
        when(messageRepository.findById(1L)).thenReturn(Optional.of(message));

        // Act
        Optional<Message> result = messageService.getMessageById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(message.getContent(), result.get().getContent());
        verify(messageRepository, times(1)).findById(1L);
    }

    @Test
    void testUpdateReadStatusOfMessage() {
        // Arrange
        message.setIsRead(true);
        when(messageRepository.save(message)).thenReturn(message);

        // Act
        Message updatedMessage = messageService.updateReadStatusOfMessage(message);

        // Assert
        assertNotNull(updatedMessage);
        assertTrue(updatedMessage.getIsRead());
        verify(messageRepository, times(1)).save(message);
    }

    @Test
    void testUpdateReadStatusOfMessagesReceivedByEmployee() {
        // Arrange
        int updatedCount = 5;
        when(messageRepository.updateReadStatusOfReceivedMessagesFromEmployee(1L, 2L, 101L)).thenReturn(updatedCount);

        // Act
        int result = messageService.updateReadStatusOfMessagesReceivedByEmployee(1L, 2L, 101L);

        // Assert
        assertEquals(5, result);
        verify(messageRepository, times(1)).updateReadStatusOfReceivedMessagesFromEmployee(1L, 2L, 101L);
    }
}
