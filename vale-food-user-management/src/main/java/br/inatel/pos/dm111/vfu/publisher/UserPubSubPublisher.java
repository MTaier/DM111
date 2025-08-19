package br.inatel.pos.dm111.vfu.publisher;

import br.inatel.pos.dm111.vfu.persistence.user.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Profile("local")
@Component
public class UserPubSubPublisher implements AppPublisher {

    private static final Logger log = LoggerFactory.getLogger(UserPubSubPublisher.class);

    private final Publisher publisher;
    private final ObjectMapper objectMapper;

    public UserPubSubPublisher(Publisher publisher, ObjectMapper objectMapper) {
        this.publisher = publisher;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean publishCreated(User user) {
        var event = buildEvent(user, Event.EventType.ADDED);
        return publishEvent(event);
    }

    private boolean publishEvent(Event event) {

        try {
            var convertedEvent = objectMapper.writeValueAsString(event);
            var data = ByteString.copyFromUtf8(convertedEvent);

            var pubSubMessage = PubsubMessage.newBuilder().setData(data).build();
            ApiFuture<String> result = publisher.publish(pubSubMessage);

            var messageId = result.get();
            log.info("Event was successfully published into pubSub. MessageId: {}", messageId);

            return true;
        } catch (JsonProcessingException |
                 InterruptedException |
                 ExecutionException e) {
            log.error("Failure to publish the event into pubSub topic.", e);
        }

        return false;
    }
}
