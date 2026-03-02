package com.warmplace.consumer;

import com.warmplace.document.ChatMessageDocument;
import com.warmplace.model.ChatMessage;
import com.warmplace.repository.ChatMessageMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageMongoRepository chatMessageMongoRepository;

    @KafkaListener(topics = "${kafka.topic.chat}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenChat(ChatMessage message) {
        log.info("Received chat message: {} from {} to {}", 
            message.getMessageId(), message.getSenderId(), message.getReceiverId());
        
        if ((message.getType() == null || message.getType() == ChatMessage.MessageType.CHAT) 
            && message.getReceiverId() != null 
            && !message.getReceiverId().isBlank()) {
            
            ChatMessageDocument document = convertToDocument(message);
            chatMessageMongoRepository.save(document);
            log.info("Chat message saved to MongoDB");
            
            messagingTemplate.convertAndSend(
                "/queue/" + message.getReceiverId() + "/messages",
                message
            );
            log.info("Message forwarded to user: {}", message.getReceiverId());
        } else {
            log.warn("Message skipped - type: {}, receiverId: {}", message.getType(), message.getReceiverId());
        }
    }

    @KafkaListener(topics = "${kafka.topic.group-chat}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenGroupChat(ChatMessage message) {
        log.info("Received group chat message: {} for room {}", 
            message.getMessageId(), message.getRoomId());
        
        if (message.getRoomId() != null && !message.getRoomId().isBlank()) {
            
            if (message.getType() == null || message.getType() == ChatMessage.MessageType.CHAT) {
                ChatMessageDocument document = convertToDocument(message);
                chatMessageMongoRepository.save(document);
                log.info("Group chat message saved to MongoDB");
            }
            
            messagingTemplate.convertAndSend(
                "/topic/group/" + message.getRoomId(),
                message
            );
            log.info("Group message forwarded to room: {}", message.getRoomId());
        }
    }

    private ChatMessageDocument convertToDocument(ChatMessage message) {
        LocalDateTime timestamp = message.getTimestamp() != null ? message.getTimestamp() : LocalDateTime.now();
        
        return ChatMessageDocument.builder()
                .messageId(message.getMessageId())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .receiverId(message.getReceiverId())
                .content(message.getContent())
                .type(message.getType() != null ? 
                    ChatMessageDocument.MessageType.valueOf(message.getType().name()) : 
                    ChatMessageDocument.MessageType.CHAT)
                .timestamp(timestamp)
                .roomId(message.getRoomId())
                .build();
    }
}
