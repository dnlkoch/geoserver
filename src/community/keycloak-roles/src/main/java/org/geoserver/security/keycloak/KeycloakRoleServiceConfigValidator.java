/* (c) 2026 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.keycloak;

import org.geoserver.security.GeoServerSecurityManager;
import org.geoserver.security.config.SecurityRoleServiceConfig;
import org.geoserver.security.validation.SecurityConfigException;
import org.geoserver.security.validation.SecurityConfigValidator;

/** Validates a {@link KeycloakRoleServiceConfig} to make sure all required fields are provided. */
public class KeycloakRoleServiceConfigValidator extends SecurityConfigValidator {

    public KeycloakRoleServiceConfigValidator(GeoServerSecurityManager securityManager) {
        super(securityManager);
    }

    @Override
    public void validateAddRoleService(SecurityRoleServiceConfig config) throws SecurityConfigException {
        super.validateAddRoleService(config);
        validateKeycloakConfig(config);
    }

    @Override
    public void validateModifiedRoleService(SecurityRoleServiceConfig config, SecurityRoleServiceConfig oldConfig)
            throws SecurityConfigException {
        super.validateModifiedRoleService(config, oldConfig);
        validateKeycloakConfig(config);
    }

    private void validateKeycloakConfig(SecurityRoleServiceConfig config) throws SecurityConfigException {
        if (!(config instanceof KeycloakRoleServiceConfig kcConfig)) {
            return;
        }

        if (isBlank(kcConfig.getServerUrl())) {
            throw createSecurityException("serverUrlRequired");
        }
        if (isBlank(kcConfig.getRealm())) {
            throw createSecurityException("realmRequired");
        }
        if (isBlank(kcConfig.getServiceAccountClientId())) {
            throw createSecurityException("serviceAccountClientIdRequired");
        }
        if (isBlank(kcConfig.getClientSecret())) {
            throw createSecurityException("clientSecretRequired");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
