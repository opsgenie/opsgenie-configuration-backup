package com.opsgenie.tools.backup.exporters;

import com.opsgenie.tools.backup.dto.PolicyConfig;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retrieval.DeprecatedPolicyOrderRetriever;
import com.opsgenie.tools.backup.util.BackupUtils;

import java.io.PrintWriter;

public class DeprecatedPolicyOrderExporter extends BaseExporter<PolicyConfig> {
    private String fileName = "PolicyOrders";

    public DeprecatedPolicyOrderExporter(String backupRootDirectory) {
        super(backupRootDirectory, "orders");
    }

    @Override
    protected EntityRetriever<PolicyConfig> initializeEntityRetriever() {
        return new DeprecatedPolicyOrderRetriever();
    }

    @Override
    protected String getEntityFileName(PolicyConfig alertPolicy) {
        return fileName;
    }

    @Override
    public void export() {
        try {
            fileName = getExportDirectory().getAbsolutePath() + "/" + fileName + ".json";
            PrintWriter writer = new PrintWriter(fileName, "UTF-8");
            writer.print(BackupUtils.toJson(initializeEntityRetriever().retrieveEntities()));
            writer.close();
            logger.info("Policy orders file written.");
        } catch (Exception e) {
            logger.error("Error at writing policy orders, fileName=" + fileName, e);
            return;
        }
    }
}
