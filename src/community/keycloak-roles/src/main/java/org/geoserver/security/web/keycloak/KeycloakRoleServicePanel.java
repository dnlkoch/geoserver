/* (c) 2026 Open Source Geospatial Foundation - all rights reserved
 * This code is licensed under the GPL 2.0 license, available at the root
 * application directory.
 */
package org.geoserver.security.web.keycloak;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.geoserver.security.keycloak.KeycloakClientHelper;
import org.geoserver.security.keycloak.KeycloakRoleService;
import org.geoserver.security.keycloak.KeycloakRoleServiceConfig;
import org.geoserver.security.web.role.RoleServicePanel;
import org.geoserver.web.GeoServerBasePage;
import org.geoserver.web.wicket.HelpLink;
import org.geotools.util.logging.Logging;

import java.io.Serial;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.geoserver.web.util.WebUtils.IsWicketCssFileEmpty;

/**
 * Wicket panel for configuring the Keycloak role service. Provides form fields for all connection parameters and a
 * "Synchronize Roles" button that triggers an immediate sync.
 */
public class KeycloakRoleServicePanel extends RoleServicePanel<KeycloakRoleServiceConfig> {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = Logging.getLogger(KeycloakRoleServicePanel.class);

    private static final boolean isCssEmpty = IsWicketCssFileEmpty(KeycloakRoleServicePanel.class);

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        // if the panel-specific CSS file contains actual css then have the browser load the css
        if (!isCssEmpty) {
            response.render(CssHeaderItem.forReference(
                    new PackageResourceReference(getClass(), getClass().getSimpleName() + ".css")));
        }
    }

    public KeycloakRoleServicePanel(String id, IModel<KeycloakRoleServiceConfig> model) {
        super(id, model);

        add(new TextField<String>("serverUrl").setRequired(true));
        add(new HelpLink("serverUrlHelp", this).setDialog(dialog));
        add(new TextField<String>("realm").setRequired(true));
        add(new HelpLink("realmHelp", this).setDialog(dialog));
        add(new TextField<String>("clientId"));
        add(new HelpLink("clientIdHelp", this).setDialog(dialog));

        add(new TextField<String>("serviceAccountClientId").setRequired(true));
        add(new HelpLink("serviceAccountClientIdHelp", this).setDialog(dialog));
        PasswordTextField secretField = new PasswordTextField("clientSecret");
        secretField.setResetPassword(false);
        add(secretField);
        add(new HelpLink("clientSecretHelp", this).setDialog(dialog));

        add(new AjaxSubmitLink("testConnection") {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                try {
                    KeycloakRoleServiceConfig cfg = model.getObject();
                    try (KeycloakClientHelper client = new KeycloakClientHelper(cfg)) {
                        client.testConnection();
                    }
                    info(getString("connectionSuccessful"));
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Keycloak connection test failed", e);
                    error(getString("connectionFailed") + ": " + e.getMessage());
                }
                addFeedbackPanels(target);
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                addFeedbackPanels(target);
            }
        });

        add(new AjaxSubmitLink("synchronizeRoles") {
            @Serial
            private static final long serialVersionUID = 1L;

            @Override
            protected void onSubmit(AjaxRequestTarget target) {
                try {
                    KeycloakRoleServiceConfig cfg = model.getObject();
                    String serviceName = cfg.getName();
                    if (serviceName != null) {
                        // Try to load the existing service and re-sync
                        KeycloakRoleService roleService =
                                (KeycloakRoleService) getSecurityManager().loadRoleService(serviceName);
                        roleService.synchronizeRoles();
                        info(getString("synchronizeSuccess")
                                + " ("
                                + roleService.getRoleCount()
                                + " "
                                + getString("rolesLoaded")
                                + ")");
                    } else {
                        // Service not saved yet – do an ad-hoc fetch to show the user it works
                        try (KeycloakClientHelper client = new KeycloakClientHelper(cfg)) {
                            int count = client.fetchRoleNames().size();
                            info(getString("synchronizeSuccess") + " (" + count + " " + getString("rolesLoaded") + ")");
                        }
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Keycloak role synchronization failed", e);
                    error(getString("synchronizeFailed") + ": " + e.getMessage());
                }
                addFeedbackPanels(target);
            }

            @Override
            protected void onError(AjaxRequestTarget target) {
                addFeedbackPanels(target);
            }
        });
    }

    protected void addFeedbackPanels(AjaxRequestTarget target) {
        GeoServerBasePage page = findParent(GeoServerBasePage.class);
        if (page != null) {
            page.addFeedbackPanels(target);
        }
    }
}
