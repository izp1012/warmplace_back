package com.warmplace.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Value("${kafka.topic.chat}")
    private String chatTopic;

    @Value("${kafka.topic.group-chat}")
    private String groupChatTopic;

    @Bean
    public NewTopic chatTopic() {
        return TopicBuilder.name(chatTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic groupChatTopic() {
        return TopicBuilder.name(groupChatTopic)
                .partitions(3)
                .replicas(1)
                .build();
    }
}
