package com.opsgenie.tools.backup;

import com.opsgenie.tools.backup.importers.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationImporter extends BaseBackup {
    private static List<Importer> importers;
    private final Logger logger = LoggerFactory.getLogger(ConfigurationImporter.class);
    private ImportConfig config;

    ConfigurationImporter(BackupProperties backupProperties, ImportConfig config) throws FileNotFoundException, UnsupportedEncodingException, GitAPIException {
        super(backupProperties);
        if (config == null) {
            logger.warn("Config object is null. Default Import configs will be used.");
            this.config = new ImportConfig();
        } else {
            this.config = config;
        }
    }

    /**
     * This is the main restore(import) method. This method imports opsgenie configuration from local
     * folder or remote git. If git is enabled from BackupProperties parameters it will import those
     * configurations from remote git.
     */
    void restore() throws GitAPIException, InterruptedException {
        if (getBackupProperties().isGitEnabled()) {
            cloneGit(getBackupProperties());
        }

        init();

        logger.info("Importing configs!");
        for (Importer importer : importers) {
            importer.restore();
        }
        logger.info("Finished!");
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
        importers = new ArrayList<Importer>();
        //importers.add(new CustomUserRoleImporter(rootPath, config.isAddNewCustomUserRoles(), config.isUpdateExistingCustomUserRoles()));
        //importers.add(new UserImporter(rootPath, config.isAddNewUsers(), config.isUpdateExistingUsers()));
        importers.add(new TeamImporter(rootPath, config.isAddNewTeams(), config.isUpdateExistingTeams()));
        //importers.add(new ScheduleTemplateImporter(rootPath, config.isAddNewSchedules(), config.isUpdateExistingSchedules()));
        //importers.add(new EscalationImporter(rootPath, config.isAddNewEscalations(), config.isUpdateExistingEscalations()));
       // importers.add(new ScheduleImporter(rootPath, config.isAddNewSchedules(), config.isUpdateExistingSchedules()));
        //importers.add(new UserForwardingImporter(rootPath, config.isAddNewUserForwarding(), config.isUpdateExistingUserForwarding()));
        importers.add(new DeprecatedPolicyImporter(rootPath, config.isAddNewPolicies(), config.isUpdateExistingPolicies()));
        //importers.add(new IntegrationImporter(rootPath, config.isAddNewIntegrations(), config.isUpdateExistingIntegrations()));
        importers.add(new PolicyImporter(rootPath, config.isAddNewPoliciesV2(), config.isUpdateExistingPoliciesV2()));
        importers.add(new MaintenanceImporter(rootPath, config.isAddNewMaintenance(), config.isUpdateExistingMaintenance()));

    }
}
