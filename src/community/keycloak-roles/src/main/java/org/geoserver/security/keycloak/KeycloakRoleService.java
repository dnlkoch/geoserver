/* (c) 2026 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.keycloak;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;
import org.geoserver.platform.resource.Resource;
import org.geoserver.security.GeoServerRoleStore;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.impl.AbstractRoleService;
import org.geoserver.security.impl.GeoServerRole;
import org.geotools.util.logging.Logging;

/**
 * A read-only {@link org.geoserver.security.GeoServerRoleService} that synchronizes roles from a Keycloak server.
 *
 * <p>Depending on the configuration the service fetches either <em>realm-level</em> roles or <em>client-level</em>
 * roles from Keycloak via the Admin REST API.
 *
 * <p>Synchronized roles are persisted to a local file ({@value #ROLES_FILE}) inside the service's configuration
 * directory so that they are available immediately on startup without requiring a network call to Keycloak. A fresh
 * synchronization is only triggered when the user explicitly synchronizes them in the admin UI.
 */
public class KeycloakRoleService extends AbstractRoleService {

    private static final Logger LOGGER = Logging.getLogger(KeycloakRoleService.class);

    /** Name of the file that caches the synchronized role names. */
    static final String ROLES_FILE = "roles.txt";

    private KeycloakRoleServiceConfig config;

    @Override
    public void initializeFromConfig(SecurityNamedServiceConfig config) throws IOException {
        super.initializeFromConfig(config);
        this.config = (KeycloakRoleServiceConfig) config;
        deserialize();
    }

    /** This is a read-only service – no store can be created. */
    @Override
    public boolean canCreateStore() {
        return false;
    }

    /** Read-only – returns {@code null}. */
    @Override
    public GeoServerRoleStore createStore() throws IOException {
        return null;
    }

    /**
     * Loads previously synchronized roles from the local cache file into the in-memory role maps.
     *
     * <p>If no cache file exists yet (first use before any synchronization), the role maps remain empty.
     */
    @Override
    protected void deserialize() throws IOException {
        clearMaps();

        Resource rolesResource = getRolesResource();
        if (rolesResource == null || rolesResource.getType() == Resource.Type.UNDEFINED) {
            LOGGER.info("No cached roles file found for service '" + getName()
                    + "' – run 'Synchronize Roles' to fetch from Keycloak.");
            return;
        }

        try (InputStream in = rolesResource.in()) {
            String content = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            for (String line : content.lines().toList()) {
                String roleName = line.trim();
                if (!roleName.isEmpty()) {
                    helper.roleMap.put(roleName, new GeoServerRole(roleName));
                }
            }
            // TODO Needed ? -> helper.user_roleMap
            LOGGER.info("Loaded " + helper.roleMap.size() + " cached role(s) for service '" + getName() + "'");
        }
    }

    /**
     * Connects to Keycloak, fetches role names, persists them to the local cache file and reloads the in-memory role
     * maps via {@link #load()}.
     */
    public void synchronizeRoles() throws IOException {
        LOGGER.info("Synchronizing roles from Keycloak ("
                + config.getServerUrl()
                + ", realm="
                + config.getRealm()
                + ", clientId="
                + (config.getClientId() != null ? config.getClientId() : "<realm roles>")
                + ")");

        List<String> roleNames;
        try (KeycloakClientHelper client = new KeycloakClientHelper(config)) {
            roleNames = client.fetchRoleNames();
        } catch (Exception e) {
            throw new IOException("Error fetching roles from Keycloak", e);
        }

        LOGGER.info("Fetched " + roleNames.size() + " role(s) from Keycloak – persisting to cache file");
        persistRoles(roleNames);

        // Reload from the persisted file so the in-memory state is consistent
        load();
    }

    /** Writes the given role names to the local cache file, one role per line. */
    private void persistRoles(List<String> roleNames) throws IOException {
        Resource rolesResource = getRolesResource();
        if (rolesResource == null) {
            throw new IOException("Cannot determine config root for service '" + getName() + "'");
        }
        try (OutputStream out = rolesResource.out()) {
            out.write(String.join("\n", roleNames).getBytes(StandardCharsets.UTF_8));
        }
    }

    /** Returns the {@link Resource} pointing to the local roles cache file. */
    private Resource getRolesResource() throws IOException {
        Resource configRoot = getConfigRoot();
        return configRoot != null ? configRoot.get(ROLES_FILE) : null;
    }
}
