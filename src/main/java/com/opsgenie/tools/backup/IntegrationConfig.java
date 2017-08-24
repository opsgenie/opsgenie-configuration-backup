package com.opsgenie.tools.backup;

import com.opsgenie.client.model.ActionCategorizedList;
import com.opsgenie.client.model.Integration;

public class IntegrationConfig {

    private Integration integration;
    private ActionCategorizedList integrationActions;

    public Integration getIntegration() {
        return integration;
    }

    public void setIntegration(Integration integration) {
        this.integration = integration;
    }

    public ActionCategorizedList getIntegrationActions() {
        return integrationActions;
    }

    public void setIntegrationActions(ActionCategorizedList integrationActions) {
        this.integrationActions = integrationActions;
    }
}
