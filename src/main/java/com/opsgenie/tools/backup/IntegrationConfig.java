package com.opsgenie.tools.backup;

import com.opsgenie.client.model.ActionCategorized;
import com.opsgenie.client.model.Integration;

public class IntegrationConfig {

    private Integration integration;
    private ActionCategorized integrationActions;

    public Integration getIntegration() {
        return integration;
    }

    public void setIntegration(Integration integration) {
        this.integration = integration;
    }

    public ActionCategorized getIntegrationActions() {
        return integrationActions;
    }

    public void setIntegrationActions(ActionCategorized integrationActions) {
        this.integrationActions = integrationActions;
    }
}
