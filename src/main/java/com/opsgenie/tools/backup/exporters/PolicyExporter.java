package com.opsgenie.tools.backup.exporters;

import com.opsgenie.oas.sdk.ApiException;
import com.opsgenie.oas.sdk.model.AlertPolicy;
import com.opsgenie.oas.sdk.model.ModifyAlertPolicy;
import com.opsgenie.oas.sdk.model.Recipient;
import com.opsgenie.tools.backup.EntityListService;

import java.util.List;

public class PolicyExporter extends BaseExporter<AlertPolicy> {

    public PolicyExporter(String backupRootDirectory) {
        super(backupRootDirectory, "policies");
    }

    @Override
    protected String getEntityFileName(AlertPolicy alertPolicy) {
        return alertPolicy.getId();
    }

    @Override
    protected List<AlertPolicy> retrieveEntities() throws ApiException {
        final List<AlertPolicy> policies = EntityListService.listPolicies();
        for (AlertPolicy policy : policies) {
            if (policy instanceof ModifyAlertPolicy) {
                ModifyAlertPolicy modifyAlertPolicy = (ModifyAlertPolicy) policy;
                for (Recipient recipient : modifyAlertPolicy.getRecipients()) {
                    recipient.setId(null);
                }
            }
        }
        return policies;
    }
}
