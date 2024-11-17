package com.software.upskilled.repository;

import com.software.upskilled.Entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long>
{
    @Query("from Message where sender.id = :senderId and course.id = :courseId order by sentAt desc ")
    public Optional<List<Message>> getSentMessagesForEmployee(@Param("senderId") long senderId, @Param("courseId") long courseId);

    @Query("from Message where recipient.id = :recipientId and course.id = :courseId order by sentAt desc")
    public Optional<List<Message>> getReceivedMessagesForEmployee(@Param("recipientId") long recipientId, @Param("courseId") long courseId);

    //Employees act as a sender in this instance
    @Query("select distinct mssg.sender.id from Message mssg where mssg.recipient.id = :recipientId and mssg.course.id =:courseId")
    public List<Long> getUniqueEmployeeSenderListForInstructor(@Param("recipientId") long recipientId, @Param("courseId") long courseId );

    //Employees are the recipient in this instance
    @Query("select distinct mssg.recipient.id from Message mssg where mssg.sender.id = :senderId and mssg.course.id = :courseId")
    public List<Long> getUniqueEmployeeRecipientListFromInstructor( @Param("senderId") long senderId, @Param("courseId") long courseId );

    public Message getMessageById(long id);

    @Transactional
    @Modifying( flushAutomatically = true )
    @Query("update Message mssg set mssg.isRead = true where mssg.recipient.id = :recipientId and mssg.sender.id = :senderId and mssg.course.id = :courseId and mssg.isRead = false")
    public int updateReadStatusOfReceivedMessagesFromEmployee( @Param("recipientId") Long recipientId, @Param("senderId") Long senderId,
                                                              @Param("courseId") Long courseId );
}
