package com.opsgenie.tools.backup.exporters;

import com.opsgenie.client.ApiException;
import com.opsgenie.client.api.IntegrationApi;
import com.opsgenie.client.model.ActionCategorizedList;
import com.opsgenie.client.model.IntegrationMeta;
import com.opsgenie.client.model.ListIntegrationPayload;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class IntegrationActionExporter extends BaseExporter<ActionCategorizedList> {

    private static IntegrationApi integrationApi = new IntegrationApi();

    public IntegrationActionExporter(String backupRootDirectory) {
        super(backupRootDirectory, "integration-actions");
    }

    @Override
    protected String getBeanFileName(ActionCategorizedList actions) {
        return actions.getParent().getId() + "-actions";
    }

    @Override
    protected List<ActionCategorizedList> retrieveEntities() throws ParseException, IOException, ApiException {
        final List<IntegrationMeta> integrationMetaList = integrationApi.listIntegrations(new ListIntegrationPayload()).getData();
        List<ActionCategorizedList> integrationActions = new ArrayList<ActionCategorizedList>();
        for (IntegrationMeta meta : integrationMetaList) {
            final ActionCategorizedList integrationAction = integrationApi.getIntegrationActions(meta.getId()).getData();
            integrationActions.add(integrationAction);
        }
        return integrationActions;
    }
}
