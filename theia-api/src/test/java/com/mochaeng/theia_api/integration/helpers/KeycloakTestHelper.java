package com.mochaeng.theia_api.integration.helpers;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.vavr.control.Try;
import java.util.Collections;
import java.util.List;
import org.keycloak.representations.idm.*;

public class KeycloakTestHelper {

    private static final String REALM_NAME = "testrealm";

    public static void setupRealm(KeycloakContainer keycloak) {
        var realmRepresentation = createRealm(REALM_NAME, 3600);

        var admin = keycloak.getKeycloakAdminClient();
        admin.realms().create(realmRepresentation);

        var roleRepresentation = createRole(
            "uploader",
            "Allowed to upload documents"
        );

        var clientRepresentation = createClient(
            "test-client",
            "test-secret",
            "openid-connect"
        );

        var userRepresentation = createUser(
            "uploaderUser",
            "uploader@test.com"
        );

        var credentialRepresentation = createCredential("password");

        var realmResource = admin.realm(REALM_NAME);
        realmResource.roles().create(roleRepresentation);

        var userId = Try.withResources(() ->
            realmResource.users().create(userRepresentation)
        )
            .of(response ->
                response.getLocation().getPath().replaceAll(".*/([^/]+)$", "$1")
            )
            .get();

        realmResource.clients().create(clientRepresentation).close();

        realmResource
            .users()
            .get(userId)
            .resetPassword(credentialRepresentation);

        var uploaderRole = realmResource
            .roles()
            .get("uploader")
            .toRepresentation();

        realmResource
            .users()
            .get(userId)
            .roles()
            .realmLevel()
            .add(Collections.singletonList(uploaderRole));
    }

    private static RealmRepresentation createRealm(
        String realmName,
        Integer tokenLifeSpan
    ) {
        var realm = new RealmRepresentation();
        realm.setRealm(realmName);
        realm.setEnabled(true);
        realm.setAccessTokenLifespan(tokenLifeSpan);
        return realm;
    }

    private static RoleRepresentation createRole(
        String roleName,
        String description
    ) {
        var role = new RoleRepresentation();
        role.setName(roleName);
        role.setDescription(description);
        return role;
    }

    private static ClientRepresentation createClient(
        String id,
        String secret,
        String protocol
    ) {
        var client = new ClientRepresentation();
        client.setClientId(id);
        client.setSecret(secret);
        client.setEnabled(true);
        client.setPublicClient(false);
        client.setDirectAccessGrantsEnabled(true);
        client.setServiceAccountsEnabled(false);
        client.setStandardFlowEnabled(true);
        client.setRedirectUris(List.of("*"));
        client.setWebOrigins(List.of("*"));
        client.setProtocol(protocol);
        return client;
    }

    private static UserRepresentation createUser(
        String username,
        String email
    ) {
        var user = new UserRepresentation();
        user.setUsername(username);
        user.setFirstName("firstName");
        user.setLastName("lastName");
        user.setEmail(email);
        user.setEnabled(true);
        user.setEmailVerified(true);
        return user;
    }

    private static CredentialRepresentation createCredential(String password) {
        var credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);
        credential.setTemporary(false);
        return credential;
    }
}
