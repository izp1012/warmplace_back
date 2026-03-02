package com.warmplace.repository;

import com.warmplace.document.ChatMessageDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageMongoRepository extends MongoRepository<ChatMessageDocument, String> {

    List<ChatMessageDocument> findByRoomIdOrderByTimestampAsc(String roomId);
}

