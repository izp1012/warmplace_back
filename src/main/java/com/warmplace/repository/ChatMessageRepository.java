package com.warmplace.repository;

import com.warmplace.entity.ChatMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    List<ChatMessageEntity> findByRoomIdOrderByTimestampAsc(String roomId);
    List<ChatMessageEntity> findBySenderIdAndReceiverIdOrReceiverIdAndSenderIdOrderByTimestampDesc(
        String senderId1, String receiverId1, String senderId2, String receiverId2);
    List<ChatMessageEntity> findByReceiverIdOrSenderIdOrderByTimestampDesc(String receiverId, String senderId);
}
