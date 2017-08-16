package com.opsgenie.tools.backup;

import com.opsgenie.tools.backup.importers.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

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
        initializeImporters(rootPath);
    }

    private void initializeImporters(String rootPath) {
        importers = new ArrayList<com.opsgenie.tools.backup.importers.Importer>();
            importers.add(new UserImporter(rootPath, config.isAddNewUsers(), config.isUpdateExistingUsers()));

            importers.add(new TeamImporter(rootPath, config.isAddNewTeams(), config.isUpdateExistingTeams()));

            importers.add(new ScheduleTemplateImporter(rootPath, config.isAddNewSchedules(), config.isUpdateExistingSchedules()));

            importers.add(new EscalationImporter(rootPath, config.isAddNewEscalations(), config.isUpdateExistingEscalations()));

            importers.add(new ScheduleImporter(rootPath, config.isAddNewSchedules(), config.isUpdateExistingSchedules()));

            importers.add(new UserNotificationImporter(rootPath, config.isAddNewNotifications(), config.isUpdateExistingNotifications()));

            importers.add(new TeamRoutingRuleImporter(rootPath, config.isAddNewTeamRoutingRules(), config.isUpdateExistingTeamRoutingRules()));

            importers.add(new UserForwardingImporter(rootPath, config.isAddNewUserForwarding(), config.isUpdateExistingUserForwarding()));
            importers.add(new ScheduleOverrideImporter(rootPath, config.isAddNewScheduleOverrides(), config.isUpdateExistingScheduleOverrides()));
            importers.add(new PolicyImporter(rootPath, config.isAddNewPolicies(), config.isUpdateExistingPolicies()));
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
