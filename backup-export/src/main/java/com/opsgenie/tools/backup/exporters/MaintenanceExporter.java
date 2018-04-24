package com.opsgenie.tools.backup.exporters;

import com.opsgenie.oas.sdk.model.Maintenance;
import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retrieval.MaintenanceRetriever;

/**
 * @author Zeynep Sengil
 * @version 20.04.2018 15:48
 */
public class MaintenanceExporter extends BaseExporter<Maintenance> {

    public MaintenanceExporter(String backupRootDirectory) {
        super(backupRootDirectory, "maintenance");
    }


    @Override
    protected EntityRetriever<Maintenance> initializeEntityRetriever() {
        return new MaintenanceRetriever();
    }

    @Override
    protected String getEntityFileName(Maintenance maintenance) {
        return maintenance.getId();
    }
}
