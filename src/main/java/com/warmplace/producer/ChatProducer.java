package com.warmplace.producer;

import com.warmplace.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatProducer {

    private final KafkaTemplate<String, ChatMessage> kafkaTemplate;

    @Value("${kafka.topic.chat}")
    private String chatTopic;

    @Value("${kafka.topic.group-chat}")
    private String groupChatTopic;

    public void sendChatMessage(ChatMessage message) {
        message.setMessageId(UUID.randomUUID().toString());
        
        String topic = message.getRoomId() != null ? groupChatTopic : chatTopic;
        String key = message.getRoomId() != null ? message.getRoomId() : message.getReceiverId();

        CompletableFuture<SendResult<String, ChatMessage>> future = 
            kafkaTemplate.send(topic, key, message);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Message sent successfully: {} to topic {}", 
                    message.getMessageId(), topic);
            } else {
                log.error("Failed to send message: {}", ex.getMessage());
            }
        });
    }

    public void sendToUser(String receiverId, ChatMessage message) {
        message.setMessageId(UUID.randomUUID().toString());
        
        kafkaTemplate.send(chatTopic, receiverId, message)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Direct message sent to {}: {}", receiverId, message.getMessageId());
                } else {
                    log.error("Failed to send direct message: {}", ex.getMessage());
                }
            });
    }

    public void sendToGroup(String roomId, ChatMessage message) {
        message.setMessageId(UUID.randomUUID().toString());
        
        kafkaTemplate.send(groupChatTopic, roomId, message)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Group message sent to room {}: {}", roomId, message.getMessageId());
                } else {
                    log.error("Failed to send group message: {}", ex.getMessage());
                }
            });
    }
}
