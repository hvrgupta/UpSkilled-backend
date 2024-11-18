package com.software.upskilled.Entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {
    /**
     * The Message class represents a communication message between users within the system.
     * It is used to send and receive messages between instructors and employees, or any other users.
     *
     * Fields:
     * - sender: The user who sent the message (instructor/employee).
     * - recipient: The user who receives the message (instructor/employee).
     * - content: The actual content of the message.
     * - sentAt: The timestamp when the message was sent.
     * - isRead: A flag indicating whether the message has been read by the recipient.
     * - course: An optional association with a course, in case the message pertains to a specific course.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private Users sender;  // Sender of the message

    @ManyToOne
    @JoinColumn(name = "recipient_id", nullable = false)
    private Users recipient;  // Recipient of the message

    @Column(nullable = false, length = 5000)
    private String content;  // Message content

    @CreationTimestamp
    @Column(updatable = false, name = "sent_at")
    private Date sentAt;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;  // Message status

    @ManyToOne
    @JoinColumn( name = "course_id")
    private Course course;
}
