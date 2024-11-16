package com.software.upskilled.service;

import com.software.upskilled.Entity.Message;
import com.software.upskilled.dto.CourseMessagesResponseDTO;
import com.software.upskilled.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class MessageService
{
    @Autowired
    MessageRepository messageRepository;

    public Message createNewMessage( Message message )
    {
        return messageRepository.save(message);
    }

    public Optional<List<Message>> getAllSentMessagesForUser( Long senderId, Long courseId )
    {
        //Check if sentMessages exist for the current senderId belonging to that particular course
        return messageRepository.getSentMessagesForUser( senderId, courseId );
    }

    public Optional<List<Message>> getAllReceivedMessageForUser( Long receiverId, Long courseId )
    {
        //Check if the user has received any message
        return messageRepository.getReceivedMessagesForUser( receiverId, courseId );
    }
}
