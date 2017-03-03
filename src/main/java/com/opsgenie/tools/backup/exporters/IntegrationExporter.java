package com.opsgenie.tools.backup.exporters;

import com.ifountain.opsgenie.client.util.JsonUtils;
import com.opsgenie.tools.backup.api.DeprecatedApiRequestException;
import com.opsgenie.tools.backup.api.ForbiddenApiRequestException;
import com.opsgenie.tools.backup.api.IntegrationApiRequester;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

/**
 * @author Mehmet Baris Kalkar
 * @version 12/19/16
 */
public class IntegrationExporter implements Exporter {

    private final Logger logger = LogManager.getLogger(IntegrationExporter.class);
    private String exportDirectoryRoot;
    private IntegrationApiRequester apiRequester;

    public IntegrationExporter(IntegrationApiRequester apiRequester, String backupRootDirectory) {
        this.apiRequester = apiRequester;
        exportDirectoryRoot = backupRootDirectory + "/integrations/";
    }

    @Override
    public void export() {
        try {
            logger.info("Exporting integrations");
            final List<Map<String, Object>> integrations;
            integrations = apiRequester.listIntegrations();
            for (Map integrationMeta : integrations) {
                exportIntegration(integrationMeta);
            }
        } catch (Exception e) {
            logger.error("Could not export integrations. " + e.getMessage());
        }
    }

    private void exportIntegration(Map integrationMeta) throws Exception {
        final String integrationId = integrationMeta.get("id").toString();
        final String integrationType = integrationMeta.get("type").toString();
        try {
            logger.info("Get request for integration " + integrationId);
            final Map<String, Object> integration = apiRequester.getIntegration(integrationId);
            logger.info("Exporting integration: " + integration.get("name"));
            File file = new File(exportDirectoryRoot + "/" + integrationType + "/" + integrationId);
            file.mkdirs();
            integration.remove("_readOnly");

            try {
                if (integration.get("isGlobal").equals(false)) {
                    Map<String, Object> team = (Map<String, Object>) integration.get("ownerTeam");
                    team.remove("id");
                }
            } catch (Exception e) {
                logger.error("-------------------------");
                logger.error("Could not remove team id.");
            }

            exportFile(file.getAbsolutePath() + "/integration.json", integration);
            if (isIntegrationAdvanced(integration)) {
                exportIntegrationAction(integrationId, file);
            }
        } catch (DeprecatedApiRequestException e) {
            logger.warn("Can not export " + integrationId + ". " + integrationType + " integrations are deprecated.");
        } catch (ForbiddenApiRequestException e) {
            logger.warn("Could not export " + integrationId + ". Exporting is not supported for " + integrationType);
        }
    }

    private void exportIntegrationAction(String integrationId, File file) throws Exception {
        final Map<String, Object> integrationActions = apiRequester.getIntegrationActions(integrationId);
        integrationActions.remove("_parent");


        exportFile(file.getAbsolutePath() + "/integration-actions.json", integrationActions);
    }

    private boolean isIntegrationAdvanced(Map<String, Object> integration) {
        return integration.containsKey("isAdvanced") && integration.get("isAdvanced").equals(true);
    }

    private void exportFile(String fileName, Map integration) {
        try {
            final PrintWriter writer = new PrintWriter(fileName, "UTF-8");
            writer.print(JsonUtils.toJson(integration));
            writer.close();
        } catch (Exception e) {
            logger.error("Error at writing entity, fileName=" + fileName, e);
        }
    }

}
