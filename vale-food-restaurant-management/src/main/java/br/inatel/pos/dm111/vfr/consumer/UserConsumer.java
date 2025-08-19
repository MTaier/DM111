package br.inatel.pos.dm111.vfr.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Profile("local")
@Component
public class UserConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserConsumer.class);

    private final ProjectSubscriptionName subscriptionName;
    private final ObjectMapper objectMapper;

    public UserConsumer(ProjectSubscriptionName subscriptionName, ObjectMapper objectMapper) {
        this.subscriptionName = subscriptionName;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void run() {
        log.info("Starting the consuming thread...");
        MessageReceiver receiver = (PubsubMessage message, AckReplyConsumer consumer) -> {

            var messageId = message.getMessageId();
            var dataStr = message.getData().toString(StandardCharsets.UTF_8);

            try {
                var event = objectMapper.readValue(dataStr, Event.class);
                switch (event.type()) {
                    case ADDED,
                         UPDATED,
                         DELETED-> log.info("Event received: type: {}, user id: {}, messageId: {}",
                            event.type(), event.event().id(), messageId);
                    default -> log.warn("Invalid event type published. MessageId: {}", messageId);
                }
            } catch (JsonProcessingException e) {
                log.error("Failure to convert the Event. MessageId: {}", messageId, e);
                throw new RuntimeException(e);
            }

            consumer.ack();
        };


        var subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
        subscriber.startAsync().awaitRunning();
        log.info("Waiting for messages on subscription...");
    }
}
