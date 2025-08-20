package br.inatel.pos.dm111.vfu.publisher;

import br.inatel.pos.dm111.vfu.persistence.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Profile("local")
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

        // restaurante continua recebendo o evento em /users
        var restaurantUsersUrl = restaurantUrl.endsWith("/") ? restaurantUrl + "users" : restaurantUrl + "/users";
        restTemplate.postForObject(restaurantUsersUrl, event.event(), UserEvent.class);

        // AUTH: publica o JSON completo COM password em /users
        var authUsersUrl = authUrl.endsWith("/") ? authUrl + "users" : authUrl + "/users";
        var body = new java.util.HashMap<String, Object>();
        body.put("id", user.id());
        body.put("name", user.name());
        body.put("email", user.email());
        body.put("password", user.password()); // << já criptografada pelo user-management
        body.put("type", user.type().name());
        restTemplate.postForObject(authUsersUrl, body, Void.class);

        return true;
    }
}
