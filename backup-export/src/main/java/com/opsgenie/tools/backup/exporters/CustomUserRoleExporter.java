package com.opsgenie.tools.backup.exporters;

import com.opsgenie.oas.sdk.model.CustomUserRole;
import com.opsgenie.oas.sdk.model.Escalation;
import com.opsgenie.tools.backup.retrieval.CustomUserRoleRetriever;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retrieval.EscalationRetriever;

public class CustomUserRoleExporter extends BaseExporter<CustomUserRole> {

    public CustomUserRoleExporter(String backupRootDirectory) {
        super(backupRootDirectory, "customUserRoles");
    }

    @Override
    protected EntityRetriever<CustomUserRole> initializeEntityRetriever() {
        return new CustomUserRoleRetriever();
    }

    @Override
    protected String getEntityFileName(CustomUserRole customUserRole) {
        return customUserRole.getName() + "-" + customUserRole.getId();
    }

}
