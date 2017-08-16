package com.opsgenie.tools.backup;

import com.opsgenie.tools.backup.exporters.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is base exporter class. It takes {@link BackupProperties} class inorder to set export
 * parameters.
 *
 * @author Mehmet Mustafa Demir
 */
public class ConfigurationExporter extends BaseBackup {
    private static List<Exporter> exporters;
    private final Logger logger = LogManager.getLogger(ConfigurationExporter.class);

    public ConfigurationExporter(BackupProperties backupProperties) throws FileNotFoundException, UnsupportedEncodingException, GitAPIException {
        super(backupProperties);
    }

    protected void init() {
        String rootPath = getBackupProperties().getPath() + "/OpsGenieBackups";
        File backupRootFile = new File(rootPath);
        if (backupRootFile.exists() && (backupRootFile.list() != null && backupRootFile.list().length > 0)) {
            logger.warn("Destination path " + rootPath + " already exists and is not an empty directory");
            logger.warn("Destination path " + rootPath + " will be deleted inorder to export current system");
            BackupUtils.deleteDirectory(backupRootFile);

        }
        backupRootFile.mkdirs();
        initializeExporters(rootPath);
    }

    private void initializeExporters(String rootPath) {
        exporters = new ArrayList<com.opsgenie.tools.backup.exporters.Exporter>();
        exporters.add(new UserExporter(rootPath));
        exporters.add(new UserNotificationExporter(rootPath));
        exporters.add(new TeamExporter(rootPath));
        exporters.add(new TeamRoutingRuleExporter(rootPath));
        exporters.add(new ScheduleExporter(rootPath));
        exporters.add(new EscalationExporter(rootPath));
        exporters.add(new UserForwardingExporter(rootPath));
        exporters.add(new ScheduleOverrideExporter(rootPath));
        exporters.add(new AlertPolicyExporter(rootPath));
    }

    /**
     * This is main export method. This method export opsgenie configuration to local folder. If git
     * is enabled from BackupProperties parameters it will export those configurations to remote
     * git.
     */

    public void export() throws GitAPIException {
        if (getBackupProperties().isGitEnabled()) {
            cloneGit(getBackupProperties());
        }
        init();
        logger.info("Export operation started!");
        for (Exporter exporter : exporters) {
            exporter.export();
        }
        if (getBackupProperties().isGitEnabled()) {
            logger.info("Export to remote git operation started!");
            getGit().add().addFilepattern("OpsGenieBackups").call();
            getGit().commit().setAll(true).setAllowEmpty(true).setMessage("Opsgenie Backups").setCommitter("opsgenie", "info@opsgenie.com").call();
            PushCommand pc = getGit().push();
            pc.setTransportConfigCallback(getCallBack());
            pc.setForce(true).setPushAll();
            pc.call();
            logger.info("Export to remote git operation finished!");
        }
        logger.info("Export operation finished!");
    }
}
