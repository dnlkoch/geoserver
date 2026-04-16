/* (c) 2026 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.keycloak;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.ext.ContextResolver;
import java.io.Closeable;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.geotools.util.logging.Logging;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

/** Helper that wraps the Keycloak Admin Client and provides a simple API for fetching realm or client roles. */
public class KeycloakClientHelper implements Closeable {

    private static final Logger LOGGER = Logging.getLogger(KeycloakClientHelper.class);

    private final Keycloak keycloak;
    private final Client resteasyClient;
    private final String realm;
    private final String clientId;

    /**
     * Creates a new helper from the given configuration.
     *
     * <p>A custom RESTEasy {@link Client} is built with a Jackson {@link ObjectMapper} that does <b>not</b> use
     * {@link java.util.ServiceLoader}-based module discovery. This avoids classloader conflicts with GeoServer modules
     * (e.g.&nbsp;{@code JtsModule}) that are not compatible with the Keycloak client's Jackson context.
     *
     * @param config the Keycloak role-service configuration
     */
    public KeycloakClientHelper(KeycloakRoleServiceConfig config) {
        this.realm = config.getRealm();
        this.clientId = config.getClientId();
        this.resteasyClient = buildResteasyClient();
        this.keycloak = KeycloakBuilder.builder()
                .serverUrl(config.getServerUrl())
                .realm(this.realm)
                .grantType("client_credentials")
                .clientId(config.getServiceAccountClientId())
                .clientSecret(config.getClientSecret())
                .resteasyClient(resteasyClient)
                .build();
    }

    /**
     * Creates a RESTEasy {@link Client} whose Jackson provider uses a plain {@link ObjectMapper} without
     * {@code ServiceLoader}-based module auto-discovery.
     *
     * <p>The default RESTEasy client auto-discovers its Jackson provider via {@code META-INF/services}. That provider
     * internally calls {@link ObjectMapper#findAndRegisterModules()} which triggers Jackson's
     * {@link java.util.ServiceLoader} scan. In GeoServer this picks up {@code JtsModule} which is loaded by a different
     * classloader and fails with {@code ServiceConfigurationError: not a subtype}.
     *
     * <p>RESTEasy's Jackson provider resolves its {@link ObjectMapper} via a {@link ContextResolver} if one is
     * registered. By providing a {@code ContextResolver<ObjectMapper>} that returns a clean mapper (without
     * auto-discovered modules) the problematic {@code findAndRegisterModules()} call is bypassed entirely.
     */
    private static Client buildResteasyClient() {
        // Create an ObjectMapper that does NOT call findAndRegisterModules()
        ObjectMapper mapper = new ObjectMapper();
        // https://www.keycloak.org/securing-apps/admin-client#_admin_client_compatibility
        mapper.setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // Register a ContextResolver so that RESTEasy's auto-discovered Jackson provider
        // uses our clean ObjectMapper instead of creating one with findAndRegisterModules().
        // Note: RESTEasy does not support lambdas for ContextResolver (it needs to resolve
        // the generic type parameter via reflection), so we use a concrete inner class.
        return ClientBuilder.newBuilder()
                .register(new ObjectMapperResolver(mapper))
                .build();
    }

    /**
     * Fetches role names from Keycloak. If a {@code clientId} is configured, client-level roles for that client are
     * returned; otherwise realm-level roles are returned.
     *
     * @return an unmodifiable list of role name strings
     */
    public List<String> fetchRoleNames() {
        try {
            RealmResource realmResource = keycloak.realm(realm);

            if (clientId != null && !clientId.isBlank()) {
                return fetchClientRoles(realmResource, clientId);
            } else {
                return fetchRealmRoles(realmResource);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error fetching roles from Keycloak", e);
            return Collections.emptyList();
        }
    }

    private List<String> fetchRealmRoles(RealmResource realmResource) {
        List<RoleRepresentation> roles = realmResource.roles().list();
        return roles.stream().map(RoleRepresentation::getName).toList();
    }

    private List<String> fetchClientRoles(RealmResource realmResource, String clientIdName) {
        ClientsResource clientsResource = realmResource.clients();
        List<ClientRepresentation> clients = clientsResource.findByClientId(clientIdName);
        if (clients.isEmpty()) {
            LOGGER.warning("Keycloak client '" + clientIdName + "' not found in realm '" + realm + "'");
            return Collections.emptyList();
        }

        String internalId = clients.get(0).getId();
        ClientResource clientResource = clientsResource.get(internalId);
        List<RoleRepresentation> roles = clientResource.roles().list();
        return roles.stream().map(RoleRepresentation::getName).toList();
    }

    /**
     * Tests the connection to Keycloak by requesting the realm representation.
     *
     * @throws Exception if the connection fails
     */
    public void testConnection() throws Exception {
        keycloak.realm(realm).toRepresentation();
    }

    /**
     * A concrete {@link ContextResolver} for {@link ObjectMapper}. RESTEasy requires a non-lambda implementation so it
     * can resolve the generic type parameter {@code ObjectMapper} via reflection.
     */
    private static class ObjectMapperResolver implements ContextResolver<ObjectMapper> {
        private final ObjectMapper mapper;

        ObjectMapperResolver(ObjectMapper mapper) {
            this.mapper = mapper;
        }

        @Override
        public ObjectMapper getContext(Class<?> type) {
            return mapper;
        }
    }

    @Override
    public void close() {
        try {
            keycloak.close();
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Error closing Keycloak client", e);
        }
        try {
            resteasyClient.close();
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Error closing RESTEasy client", e);
        }
    }
}
