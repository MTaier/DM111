package br.inatel.pos.dm111.vfu.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.pubsub.v1.TopicName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

@Profile("local")
@Configuration
public class PubSubConfiguration {

    @Value("${vale-food.gae.project-id}")
    private String projectId;

    @Value("${vale-food.gae.users.topic-name}")
    private String usersTopicName;

    @Bean
    public TopicName topicName() {
        return TopicName.of(projectId, usersTopicName);
    }

    @Bean
    public Publisher publisher(TopicName topicName) throws IOException {
        return Publisher.newBuilder(topicName).build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
