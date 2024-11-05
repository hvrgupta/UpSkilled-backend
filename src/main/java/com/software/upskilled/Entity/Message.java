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
}
