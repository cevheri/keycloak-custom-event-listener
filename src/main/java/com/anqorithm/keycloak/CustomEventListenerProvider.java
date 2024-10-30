package com.anqorithm.keycloak;

import java.util.Map;

import org.jboss.logging.Logger;
import org.keycloak.events.Event;
import org.keycloak.events.EventListenerProvider;
import org.keycloak.events.EventType;
import org.keycloak.events.admin.AdminEvent;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RealmProvider;
import org.keycloak.models.UserModel;

public class CustomEventListenerProvider
        implements EventListenerProvider {

    private static final Logger log = Logger.getLogger(CustomEventListenerProvider.class);

    private final KeycloakSession session;
    private final RealmProvider model;

    public CustomEventListenerProvider(KeycloakSession session) {
        this.session = session;
        this.model = session.realms();
    }

    @Override
    public void onEvent(Event event) {

        log.debugf("New %s Event", event.getType());
        log.debugf("onEvent-> %s", toString(event));

        if (EventType.REGISTER.equals(event.getType())) {

            event.getDetails().forEach((key, value) -> log.debugf("%s : %s", key, value));

            RealmModel realm = this.model.getRealm(event.getRealmId());
            UserModel user = this.session.users().getUserById(realm, event.getUserId());
            sendUserData(user);
        }

    }

    @Override
    public void onEvent(AdminEvent adminEvent, boolean b) {
        log.debug("onEvent(AdminEvent)");
        log.debugf("Resource path: %s", adminEvent.getResourcePath());
        log.debugf("Resource type: %s", adminEvent.getResourceType());
        log.debugf("Operation type: %s", adminEvent.getOperationType());
        log.debugf("AdminEvent.toString(): %s", toString(adminEvent));
        if (ResourceType.USER.equals(adminEvent.getResourceType())
                && OperationType.CREATE.equals(adminEvent.getOperationType())) {
            RealmModel realm = this.model.getRealm(adminEvent.getRealmId());
            UserModel user = this.session.users().getUserById(realm, adminEvent.getResourcePath().substring(6));

            sendUserData(user);
        }
    }

    private void sendUserData(UserModel user) {
        String data
                = "{\"id\": " + user.getId() + "\","
                + "{\"email\": " + user.getEmail() + "\","
                + "\"userName\":\"" + user.getUsername() + "\","
                + "\"firstName\":\"" + user.getFirstName() + "\","
                + "\"lastName\":\"" + user.getLastName() + "\","
                + "}";
        try {
            Client.postService(data);
            log.debug("A new user has been created and post API");
        } catch (Exception e) {
            log.errorf("Failed to call API: %s", e);
        }
    }

    @Override
    public void close() {
    }

    private String toString(Event event) {

        StringBuilder sb = new StringBuilder();
        sb.append("type=");
        sb.append(event.getType());
        sb.append(", realmId=");
        sb.append(event.getRealmId());
        sb.append(", clientId=");
        sb.append(event.getClientId());
        sb.append(", userId=");
        sb.append(event.getUserId());
        sb.append(", ipAddress=");
        sb.append(event.getIpAddress());
        if (event.getError() != null) {
            sb.append(", error=");
            sb.append(event.getError());
        }

        if (event.getDetails() != null) {
            for (Map.Entry<String, String> e : event.getDetails().entrySet()) {
                sb.append(", ");
                sb.append(e.getKey());
                if (e.getValue() == null || e.getValue().indexOf(' ') == -1) {
                    sb.append("=");
                    sb.append(e.getValue());
                } else {
                    sb.append("='");
                    sb.append(e.getValue());
                    sb.append("'");
                }
            }
        }

        return sb.toString();
    }

    private String toString(AdminEvent event) {

        RealmModel realm = this.model.getRealm(event.getRealmId());

        UserModel newRegisteredUser
                = this.session.users().getUserById(realm, event.getAuthDetails().getUserId());

        StringBuilder sb = new StringBuilder();
        sb.append("operationType=");
        sb.append(event.getOperationType());
        sb.append(", realmId=");
        sb.append(event.getAuthDetails().getRealmId());
        sb.append(", clientId=");
        sb.append(event.getAuthDetails().getClientId());
        sb.append(", userId=");
        sb.append(event.getAuthDetails().getUserId());

        if (newRegisteredUser != null) {
            sb.append(", email=");
            sb.append(newRegisteredUser.getEmail());
            sb.append(", getUsername=");
            sb.append(newRegisteredUser.getUsername());
            sb.append(", getFirstName=");
            sb.append(newRegisteredUser.getFirstName());
            sb.append(", getLastName=");
            sb.append(newRegisteredUser.getLastName());
        }
        sb.append(", ipAddress=");
        sb.append(event.getAuthDetails().getIpAddress());
        sb.append(", resourcePath=");
        sb.append(event.getResourcePath());
        if (event.getError() != null) {
            sb.append(", error=");
            sb.append(event.getError());
        }

        return sb.toString();
    }
}
