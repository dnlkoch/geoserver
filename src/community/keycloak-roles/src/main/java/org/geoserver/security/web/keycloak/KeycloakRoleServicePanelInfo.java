/* (c) 2026 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.web.keycloak;

import java.io.Serial;
import org.geoserver.security.keycloak.KeycloakRoleService;
import org.geoserver.security.keycloak.KeycloakRoleServiceConfig;
import org.geoserver.security.web.role.RoleServicePanelInfo;

/**
 * Registers the Keycloak role-service configuration panel with GeoServer's web UI.
 *
 * <p>This bean is discovered via the Spring application context and makes the "Keycloak" option available in the
 * Security &rarr; Users, Groups, and Roles &rarr; Role Services drop-down.
 */
public class KeycloakRoleServicePanelInfo
        extends RoleServicePanelInfo<KeycloakRoleServiceConfig, KeycloakRoleServicePanel> {

    @Serial
    private static final long serialVersionUID = 1L;

    public KeycloakRoleServicePanelInfo() {
        setComponentClass(KeycloakRoleServicePanel.class);
        setServiceClass(KeycloakRoleService.class);
        setServiceConfigClass(KeycloakRoleServiceConfig.class);
    }
}
