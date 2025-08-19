package br.inatel.pos.dm111.vfu.publisher;

import br.inatel.pos.dm111.vfu.persistence.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Profile("test")
@Component
public class UserHttpPublisher implements AppPublisher {

    @Value("${vale-food.restaurant.url}")
    private String restaurantUrl;

    @Value("${vale-food.auth.url}")
    private String authUrl;

    private final RestTemplate restTemplate;

    public UserHttpPublisher(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public boolean publishCreated(User user) {
        var event = buildEvent(user, Event.EventType.ADDED);

        restTemplate.postForObject(restaurantUrl, event.event(), UserEvent.class);
        restTemplate.postForObject(authUrl, event.event(), UserEvent.class);

        return true;
    }
}
