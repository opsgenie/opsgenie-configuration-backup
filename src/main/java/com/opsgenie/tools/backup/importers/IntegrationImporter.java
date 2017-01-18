package com.opsgenie.tools.backup.importers;

import com.ifountain.opsgenie.client.util.JsonUtils;
import com.opsgenie.tools.backup.BackupUtils;
import com.opsgenie.tools.backup.api.IntegrationApiRequester;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author Mehmet Baris Kalkar
 * @version 12/21/16
 */
public class IntegrationImporter implements Importer {
    private final Logger logger = LogManager.getLogger(IntegrationImporter.class);
    private File importDirectory;
    private boolean addEntityEnabled;
    private boolean updateEntityEnabled;
    private IntegrationApiRequester integrationApiRequester;
    private List<Map<String, Object>> existingIntegrations;

    public IntegrationImporter(String backupRootDirectory, String apiKey, boolean addEntityEnabled, boolean updateEntityEnabled) {
        this.addEntityEnabled = addEntityEnabled;
        this.updateEntityEnabled = updateEntityEnabled;
        this.importDirectory = new File(backupRootDirectory + "/integrations/");
        integrationApiRequester = new IntegrationApiRequester(apiKey);
    }

    public void restore() {
        logger.info("Restoring integrations operation is started");

        if (!importDirectory.exists()) {
            logger.error("Error : integration directory does not exist. Restoring integrations skipped");
            return;
        }

        try {
            existingIntegrations = integrationApiRequester.listIntegrations();
        } catch (Exception e) {
            logger.error("An error occured while listing integrations. " + e.getMessage());
            return;
        }

        final List<String> integrationDirectoryList = BackupUtils.findIntegrationDirectories(importDirectory.getAbsolutePath());
        for (String integrationDirectoryPath : integrationDirectoryList) {
            importIntegration(integrationDirectoryPath);
        }

        logger.info("Restoring integration operation is finished");
    }

    private void importIntegration(String integrationDirectoryPath) {
        final Map<String, Object> integration;
        try {
            integration = readJsonFileIntoMap(integrationDirectoryPath + "/integration.json");
        } catch (IOException e) {
            logger.warn("Could not read " + integrationDirectoryPath + "/integration.json");
            return;
        }
        for (Map<String, Object> existingIntegration : existingIntegrations) {
            if (addEntityEnabled && compareIntegrations(integration, existingIntegration) == BeanStatus.MODIFIED) {
                final Map<String, Object> updatedIntegrationMeta = updateIntegration(integration, existingIntegration.get("id").toString());
                if (checkIfIntegrationIsAdvanced(integration)) {
                    updateIntegrationAction(integrationDirectoryPath, updatedIntegrationMeta);
                }
                return;
            }
        }
        if (updateEntityEnabled) {
            final Map<String, Object> updatedIntegration = addIntegration(integration);
            if (checkIfIntegrationIsAdvanced(integration)) {
                updateIntegrationAction(integrationDirectoryPath, updatedIntegration);
            }
        }
    }

    private boolean checkIfIntegrationIsAdvanced(Map<String, Object> integration) {
        return integration.containsKey("isAdvanced") && integration.get("isAdvanced").equals(true);
    }

    private void updateIntegrationAction(String integrationDirectoryPath, Map<String, Object> updatedIntegration) {
        final String id = getIdFromIntegrationMetaMap(updatedIntegration);
        final Map<String, Object> integrationAction;
        try {
            integrationAction = readJsonFileIntoMap(integrationDirectoryPath + "/integration-actions.json");
        } catch (IOException e) {
            logger.warn("Could not read " + integrationDirectoryPath + "/integration.json");
            return;
        }
        updateIntegrationAction(integrationAction, id);
    }

    private String getIdFromIntegrationMetaMap(Map<String, Object> updatedIntegration) {
        final Map<String, Object> jsonData = (Map<String, Object>) updatedIntegration.get("data");
        return jsonData.get("id").toString();
    }

    private BeanStatus compareIntegrations(Map<String, Object> oldEntity, Map<String, Object> currentEntity) {
        if (getIntegrationName(oldEntity).equals(getIntegrationName(currentEntity))) {
            return BeanStatus.MODIFIED;
        }
        return BeanStatus.NOT_EXIST;
    }

    private Map<String, Object> readJsonFileIntoMap(String fileName) throws IOException {
        final String integrationJson;
        integrationJson = BackupUtils.readFile(fileName);
        return JsonUtils.parse(integrationJson);
    }

    private Map<String, Object> updateIntegration(Map<String, Object> integrationMap, String currentIntegrationId) {
        try {
            final Map<String, Object> integration = integrationApiRequester.updateIntegration(integrationMap, currentIntegrationId);
            logger.info(getIntegrationName(integrationMap) + " updated");
            return integration;
        } catch (Exception e) {
            logger.error("Could not update " + getIntegrationName(integrationMap) + " integration. " + e.getMessage());
        }
        return null;
    }

    private void updateIntegrationAction(Map<String, Object> integrationAction, String integrationId) {
        try {
            logger.info("Updating integration actions of " + integrationId);
            integrationApiRequester.updateIntegrationAction(integrationAction, integrationId);
        } catch (Exception e) {
            logger.error("Could not update integration action:" + e.getMessage());
        }
    }

    private Map<String, Object> addIntegration(Map<String, Object> integrationMap) {
        try {
            final Map<String, Object> integration = integrationApiRequester.createIntegration(integrationMap);
            logger.info(getIntegrationName(integrationMap) + " added");
            return integration;
        } catch (Exception e) {
            logger.error("Could not add " + getIntegrationName(integrationMap) + " integration. " + e.getMessage());
        }
        return null;
    }

    public String getIntegrationName(Map<String, Object> integration) {
        return integration.get("name").toString();
    }

}
