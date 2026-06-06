package com.tradingplatform.infrastructure.keycloak;

import com.tradingplatform.domain.User;
import com.tradingplatform.domain.port.KeycloakPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdapter implements KeycloakPort {

    private final WebClient webClient;

    @Value("${keycloak.admin.url}")
    private String keycloakUrl;

    @Value("${keycloak.admin.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.client-secret}")
    private String clientSecret;

    @Override
    public Mono<String> createUser(User user) {
        return obtainAdminToken()
                .flatMap(token -> registerUserInKeycloak(user, token))
                .doOnSuccess(v -> log.info("User created in Keycloak: {}", user.getEmail()))
                .doOnError(e -> log.error("Error creating user in Keycloak", e));
    }

    private Mono<String> obtainAdminToken() {
        return webClient.post()
                .uri(keycloakUrl + "/realms/master/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("grant_type", "client_credentials")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret))
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("access_token"));
    }

    private Mono<String> registerUserInKeycloak(User user, String token) {
        Map<String, Object> credential = Map.of(
                "type", "password",
                "value", user.getPassword().value(),
                "temporary", false
        );

        Map<String, Object> userRepresentation = Map.of(
                "username", user.getName(),
                "email", user.getEmail().value(),
                "enabled", true,
                "emailVerified", true,  // Dejarlo solo en desarrollo, despues añadir correo
                "credentials", List.of(credential)
        );

        return webClient.post()
                .uri(keycloakUrl + "/admin/realms/" + realm + "/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRepresentation)
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    // Keycloak devuelve la URL del usuario creado en el header Location
                    String location = response.getHeaders().getFirst("Location");
                    return location.substring(location.lastIndexOf("/") + 1);
                });
    }

}