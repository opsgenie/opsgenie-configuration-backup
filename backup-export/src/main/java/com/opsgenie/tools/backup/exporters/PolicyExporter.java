package com.opsgenie.tools.backup.exporters;

import com.opsgenie.tools.backup.dto.PolicyWithTeamInfo;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retrieval.PolicyRetriever;

/**
 * @author Zeynep Sengil
 * @version 19.04.2018 14:34
 */
public class PolicyExporter extends BaseExporter<PolicyWithTeamInfo> {
    public PolicyExporter(String backupRootDirectory) {
        super(backupRootDirectory, "policiesV2");
    }

    @Override
    protected EntityRetriever<PolicyWithTeamInfo> initializeEntityRetriever() {
        return new PolicyRetriever();
    }

    @Override
    protected String getEntityFileName(PolicyWithTeamInfo policyWithTeamInfo) {
        return policyWithTeamInfo.getPolicy().getId();
    }
}
