package com.tradingplatform.infrastructure.impl;

import com.tradingplatform.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakAdapter {

    private final RestClient restClient;

    @Value("${keycloak.admin.url}")
    private String keycloakUrl;

    @Value("${keycloak.admin.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.client-secret}")
    private String clientSecret;

    public String createUser(User user) {
        try {
            String token = obtainAdminToken();
            String userId = registerUserInKeycloak(user, token);
            log.info("User created in Keycloak: {}", user.getEmail());
            return userId;
        } catch (Exception e) {
            log.error("Error creating user in Keycloak", e);
            throw e;
        }
    }

    private String obtainAdminToken() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);

        Map response = restClient.post()
                .uri(keycloakUrl + "/realms/master/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(Map.class);

        return (String) response.get("access_token");
    }

    private String registerUserInKeycloak(User user, String token) {
        Map<String, Object> credential = Map.of(
                "type", "password",
                "value", user.getPassword().value(),
                "temporary", false
        );

        Map<String, Object> userRepresentation = Map.of(
                "username", user.getName(),
                "email", user.getEmail().value(),
                "enabled", true,
                "emailVerified", true,
                "credentials", List.of(credential)
        );

        var response = restClient.post()
                .uri(keycloakUrl + "/admin/realms/" + realm + "/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .body(userRepresentation)
                .retrieve()
                .toBodilessEntity();

        String location = response.getHeaders().getFirst("Location");
        return location.substring(location.lastIndexOf("/") + 1);
    }

    public void deleteUser(String keycloakId) {
        try {
            String token = obtainAdminToken();
            restClient.delete()
                    .uri(keycloakUrl + "/admin/realms/" + realm + "/users/" + keycloakId)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                    .retrieve()
                    .toBodilessEntity();
            log.info("User deleted from Keycloak: {}", keycloakId);
        } catch (Exception e) {
            log.error("Error deleting user from Keycloak", e);
            throw e;
        }
    }

}
