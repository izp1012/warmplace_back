package com.warmplace.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_messages")
public class ChatMessageDocument {
    @Id
    private String id;
    private String messageId;
    private String senderId;
    private String senderName;
    private String receiverId;
    private String content;
    private MessageType type;
    private LocalDateTime timestamp;
    private String roomId;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE,
        TYPING,
        READ
    }
}
