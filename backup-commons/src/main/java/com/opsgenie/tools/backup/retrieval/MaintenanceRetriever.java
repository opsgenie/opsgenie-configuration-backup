package com.opsgenie.tools.backup.retrieval;

import com.opsgenie.oas.sdk.api.MaintenanceApi;
import com.opsgenie.oas.sdk.model.Maintenance;
import com.opsgenie.oas.sdk.model.MaintenanceMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Zeynep Sengil
 * @version 20.04.2018 15:50
 */
public class MaintenanceRetriever  implements EntityRetriever<Maintenance>{

    private static final Logger logger = LoggerFactory.getLogger(PolicyRetriever.class);

    private static final MaintenanceApi maintenanceApi = new MaintenanceApi();

    private List<Maintenance> maintenanceList = new ArrayList<Maintenance>();


    @Override
    public List<Maintenance> retrieveEntities() {
        logger.info("Retrieving current maintenance configurations");
        List<MaintenanceMeta> metas = maintenanceApi.listMaintenance("non-expired").getData();
        retrieveMaintenance(metas);

        return maintenanceList;
    }

    private void retrieveMaintenance(List<MaintenanceMeta> maintenanceMetaList){
        for (MaintenanceMeta maintenanceMeta : maintenanceMetaList){
            Maintenance maintenance = maintenanceApi.getMaintenance(maintenanceMeta.getId()).getData();
            if (maintenance != null){
                maintenanceList.add(maintenance);
            }
        }
    }
}
