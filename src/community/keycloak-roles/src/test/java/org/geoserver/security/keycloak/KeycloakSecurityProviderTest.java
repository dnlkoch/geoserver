/* (c) 2026 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.keycloak;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import org.geoserver.security.GeoServerRoleService;
import org.junit.Test;

public class KeycloakSecurityProviderTest {

    @Test
    public void testRoleServiceClass() {
        KeycloakSecurityProvider provider = new KeycloakSecurityProvider();
        assertEquals(KeycloakRoleService.class, provider.getRoleServiceClass());
    }

    @Test
    public void testCreateRoleService() throws IOException {
        KeycloakSecurityProvider provider = new KeycloakSecurityProvider();
        GeoServerRoleService service = provider.createRoleService(null);
        assertNotNull(service);
        assertTrue(service instanceof KeycloakRoleService);
    }

    @Test
    public void testFieldsForEncryption() {
        KeycloakSecurityProvider provider = new KeycloakSecurityProvider();
        Map<Class<?>, Set<String>> fields = provider.getFieldsForEncryption();
        assertNotNull(fields);
        assertTrue(fields.containsKey(KeycloakRoleServiceConfig.class));
        assertTrue(fields.get(KeycloakRoleServiceConfig.class).contains("clientSecret"));
    }

    @Test
    public void testNoUserGroupService() {
        KeycloakSecurityProvider provider = new KeycloakSecurityProvider();
        assertFalse(provider.getUserGroupServiceClass() != null);
    }

    @Test
    public void testNoAuthenticationProvider() {
        KeycloakSecurityProvider provider = new KeycloakSecurityProvider();
        assertFalse(provider.getAuthenticationProviderClass() != null);
    }
}
