package com.software.upskilled.repository;

import com.software.upskilled.Entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long>
{
    @Query("from Message where sender.id = :senderId and course.id = :courseId order by sentAt desc ")
    public Optional<List<Message>> getSentMessagesForUser(@Param("senderId") long senderId, @Param("courseId") long courseId);

    @Query("from Message where recipient.id = :recipientId and course.id = :courseId order by sentAt asc")
    public Optional<List<Message>> getReceivedMessagesForUser(@Param("recipientId") long recipientId, @Param("courseId") long courseId);
}
