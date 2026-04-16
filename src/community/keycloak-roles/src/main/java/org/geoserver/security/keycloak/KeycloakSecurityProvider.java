/* (c) 2026 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.keycloak;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.geoserver.config.util.XStreamPersister;
import org.geoserver.security.GeoServerRoleService;
import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.GeoServerSecurityProvider;
import org.geoserver.security.config.SecurityNamedServiceConfig;
import org.geoserver.security.validation.SecurityConfigValidator;

/**
 * Registers the Keycloak role service with GeoServer's security subsystem.
 *
 * <p>This provider is picked up automatically via the Spring application context.
 */
public class KeycloakSecurityProvider extends GeoServerSecurityProvider {

    @Override
    public void configure(XStreamPersister xp) {
        super.configure(xp);
        xp.getXStream().alias("keycloakRoleService", KeycloakRoleServiceConfig.class);
    }

    @Override
    public Map<Class<?>, Set<String>> getFieldsForEncryption() {
        Map<Class<?>, Set<String>> map = new HashMap<>();
        Set<String> fields = new HashSet<>();
        fields.add("clientSecret");
        map.put(KeycloakRoleServiceConfig.class, fields);
        return map;
    }

    @Override
    public Class<? extends GeoServerRoleService> getRoleServiceClass() {
        return KeycloakRoleService.class;
    }

    @Override
    public GeoServerRoleService createRoleService(SecurityNamedServiceConfig config) throws IOException {
        return new KeycloakRoleService();
    }

    @Override
    public SecurityConfigValidator createConfigurationValidator(GeoServerSecurityManager securityManager) {
        return new KeycloakRoleServiceConfigValidator(securityManager);
    }
}
