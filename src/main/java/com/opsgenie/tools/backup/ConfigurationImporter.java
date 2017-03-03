package com.opsgenie.tools.backup;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.opsgenie.tools.backup.api.IntegrationApiRequester;
import com.opsgenie.tools.backup.importers.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


/**
 * This is the base importer class. It takes {@link BackupProperties} and {@link ImportConfig}
 * objects to set import settings.
 *
 * @author Mehmet Mustafa Demir
 */
public class ConfigurationImporter extends BaseBackup {
    private static List<Importer> importers;
    private final Logger logger = LogManager.getLogger(ConfigurationImporter.class);
    private ImportConfig config;

    public ConfigurationImporter(BackupProperties backupProperties, ImportConfig config) throws FileNotFoundException, UnsupportedEncodingException, GitAPIException {
        super(backupProperties);
        if (config == null) {
            logger.warn("Config object is null.");
            logger.warn("Default Import configs will be used.");
            this.config = new ImportConfig();
        } else {
            this.config = config;
        }
    }

    public ConfigurationImporter(BackupProperties backupProperties) throws FileNotFoundException, UnsupportedEncodingException, GitAPIException {
        super(backupProperties);
        logger.warn("Default Import configs will be used.");
        config = new ImportConfig();
    }

    protected void init() {
        String rootPath = getBackupProperties().getPath() + "/OpsGenieBackups";
        File backupRootFile = new File(rootPath);
        if (!backupRootFile.exists()) {
            RuntimeException e = new RuntimeException(rootPath + " directory is not exist");
            logger.error(rootPath + " directory is not exist", e);
            throw e;
        } else if (!backupRootFile.isDirectory()) {
            RuntimeException e = new RuntimeException(rootPath + " is not a directory!");
            logger.error(rootPath + " is not a directory!", e);
            throw e;
        }

        OpsGenieClient opsGenieClient = new OpsGenieClient();
        opsGenieClient.setApiKey(getBackupProperties().getApiKey());
        opsGenieClient.setRootUri(getBackupProperties().getOpsgenieUrl());
        initializeImporters(rootPath, opsGenieClient);
    }

    private void initializeImporters(String rootPath, OpsGenieClient opsGenieClient) {
        importers = new ArrayList<com.opsgenie.tools.backup.importers.Importer>();
        if (config.isAddNewHeartbeats() || config.isUpdateExistingHeartbeats())
            importers.add(new HeartbeatImporter(opsGenieClient, rootPath, config.isAddNewHeartbeats(), config.isUpdateExistingEscalations()));

        if (config.isAddNewUsers() || config.isUpdateExistingUsers())
            importers.add(new UserImporter(opsGenieClient, rootPath, config.isAddNewUsers(), config.isUpdateExistingUsers()));

        if (config.isAddNewTeams() || config.isUpdateExistingTeams())
            importers.add(new TeamImporter(opsGenieClient, rootPath, config.isAddNewTeams(), config.isUpdateExistingTeams()));

        if (config.isAddNewGroups() || config.isUpdateExistingGroups())
            importers.add(new GroupImporter(opsGenieClient, rootPath, config.isAddNewGroups(), config.isUpdateExistingGroups()));

        if (config.isAddNewSchedules() || config.isUpdateExistingSchedules())
            importers.add(new ScheduleTemplateImporter(opsGenieClient, rootPath, config.isAddNewSchedules(), config.isUpdateExistingSchedules()));

        if (config.isAddNewEscalations() || config.isUpdateExistingEscalations())
            importers.add(new EscalationImporter(opsGenieClient, rootPath, config.isAddNewEscalations(), config.isUpdateExistingEscalations()));

        if (config.isAddNewSchedules() || config.isUpdateExistingSchedules())
            importers.add(new ScheduleImporter(opsGenieClient, rootPath, config.isAddNewSchedules(), config.isUpdateExistingSchedules()));

        if (config.isAddNewNotifications() || config.isUpdateExistingNotifications())
            importers.add(new UserNotificationImporter(opsGenieClient, rootPath, config.isAddNewNotifications(), config.isUpdateExistingNotifications()));

        if (config.isAddNewTeamRoutingRules() || config.isUpdateExistingTeamRoutingRules())
            importers.add(new TeamRoutingRuleImporter(opsGenieClient, rootPath, config.isAddNewTeamRoutingRules(), config.isUpdateExistingTeamRoutingRules()));

        if (config.isAddNewUserForwarding() || config.isUpdateExistingUserForwarding())
            importers.add(new UserForwardingImporter(opsGenieClient, rootPath, config.isAddNewUserForwarding(), config.isUpdateExistingUserForwarding()));

        if (config.isAddNewScheduleOverrides() || config.isUpdateExistingScheduleOverrides())
            importers.add(new ScheduleOverrideImporter(opsGenieClient, rootPath, config.isAddNewScheduleOverrides(), config.isUpdateExistingScheduleOverrides()));

        if (config.isAddNewIntegrations() || config.isUpdateExistingIntegrations()) {
            final IntegrationApiRequester integrationApiRequester = new IntegrationApiRequester(opsGenieClient.getApiKey(), getBackupProperties().getOpsgenieUrl());
            importers.add(new IntegrationImporter(rootPath, integrationApiRequester, config.isAddNewIntegrations(), config.isUpdateExistingIntegrations()));
        }
    }

    /**
     * This is the main restore(import) method. This method imports opsgenie configuration from local
     * folder or remote git. If git is enabled from BackupProperties parameters it will import those
     * configurations from remote git.
     */

    public void restore() throws GitAPIException {
        if (getBackupProperties().isGitEnabled()) {
            cloneGit(getBackupProperties());
        }

        init();

        logger.info("Import operation started!");
        for (com.opsgenie.tools.backup.importers.Importer importer : importers) {
            try {
                importer.restore();
            } catch (RestoreException e) {
                logger.error("Error at restoring.", e);
            }
        }
        logger.info("Import operation finished!");
    }

    public ImportConfig getConfig() {
        return config;
    }

    public ConfigurationImporter setConfig(ImportConfig config) {
        this.config = config;
        return this;
    }
}
