package com.software.upskilled.service;

import com.software.upskilled.Entity.Message;
import com.software.upskilled.dto.CourseMessagesResponseDTO;
import com.software.upskilled.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service layer for handling messages between instructors and employees. Provides functionality to send, retrieve, and manage messages.
 * Includes methods to retrieve sent/received messages, update read statuses, and manage unique sender/recipient lists.
 * Transaction management is applied for updating the read status of messages.
 */
@Service
public class MessageService
{
    @Autowired
    MessageRepository messageRepository;

    public Message createNewMessage( Message message )
    {
        return messageRepository.save(message);
    }

    public Optional<List<Message>> getAllSentMessagesForEmployee( Long senderId, Long courseId ) {
        //Check if sentMessages exist for the current senderId belonging to that particular course
        return messageRepository.getSentMessagesForEmployee( senderId, courseId );
    }

    public Optional<List<Message>> getAllReceivedMessageForEmployee( Long receiverId, Long courseId ) {
        //Check if the user has received any message
        return messageRepository.getReceivedMessagesForEmployee( receiverId, courseId );
    }

    //Get the unique list of employee who have sent the messages to The Instructor
    public List<Long> getUniqueListOfSenderEmployeesForInstructor( Long instructorId, Long courseId ) {
        return messageRepository.getUniqueEmployeeSenderListForInstructor( instructorId, courseId );
    }

    //Get the unique list of employee who are the recipient of the messages from the Instructor
    public List<Long> getUniqueListOfRecipientEmployeesForInstructor( Long instructorId, Long courseId ) {
        return messageRepository.getUniqueEmployeeRecipientListFromInstructor( instructorId, courseId );
    }

    public Optional<Message> getMessageById( Long messageId )
    {
        return messageRepository.findById( messageId );
    }

    @Transactional
    @Modifying( flushAutomatically = true )
    public Message updateReadStatusOfMessage( Message existingMessage ) {
        return messageRepository.save( existingMessage );
    }

    public int updateReadStatusOfMessagesReceivedByEmployee( Long instructorId, Long employeeId, Long courseId ) {
        return messageRepository.updateReadStatusOfReceivedMessagesFromEmployee( instructorId, employeeId, courseId );
    }
}
