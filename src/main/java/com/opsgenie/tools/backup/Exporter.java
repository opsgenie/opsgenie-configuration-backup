package com.opsgenie.tools.backup;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.opsgenie.tools.backup.exporters.EscalationExporter;
import com.opsgenie.tools.backup.exporters.ExporterInterface;
import com.opsgenie.tools.backup.exporters.GroupExporter;
import com.opsgenie.tools.backup.exporters.HeartbeatExporter;
import com.opsgenie.tools.backup.exporters.ScheduleExporter;
import com.opsgenie.tools.backup.exporters.TeamExporter;
import com.opsgenie.tools.backup.exporters.UserExporter;

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
 * This is base exporter class. It takes {@link BackupProperties} class inorder to set export parameters.
 *
 * @author Mehmet Mustafa Demir
 */
public class Exporter extends BaseBackup {
    private static List<ExporterInterface> exporters;
    private final Logger logger = LogManager.getLogger(Exporter.class);

    public Exporter(BackupProperties backupProperties) throws FileNotFoundException, UnsupportedEncodingException, GitAPIException {
        super(backupProperties);
    }

    protected void init() {
        String rootPath = getBackupProperties().getPath() + "/OpsGenieBackups";
        File backupRootFile = new File(rootPath);
        if (backupRootFile.exists() && (backupRootFile.list() != null) && (backupRootFile.list().length > 0)) {
            logger.warn("Destination path " + rootPath + " already exists and is not an empty directory");
            logger.warn("Destination path " + rootPath + " will be deleted inorder to export current system");
            BackupUtils.deleteDirectory(backupRootFile);
            backupRootFile.mkdirs();
        } else {
            backupRootFile.mkdirs();
        }
        OpsGenieClient opsGenieClient = new OpsGenieClient();
        opsGenieClient.setApiKey(getBackupProperties().getApiKey());
        initializeExporters(rootPath, opsGenieClient);
    }

    private void initializeExporters(String rootPath, OpsGenieClient opsGenieClient) {
        exporters = new ArrayList<ExporterInterface>();
        exporters.add(new HeartbeatExporter(opsGenieClient, rootPath));
        exporters.add(new UserExporter(opsGenieClient, rootPath));
        exporters.add(new GroupExporter(opsGenieClient, rootPath));
        exporters.add(new TeamExporter(opsGenieClient, rootPath));
        exporters.add(new ScheduleExporter(opsGenieClient, rootPath));
        exporters.add(new EscalationExporter(opsGenieClient, rootPath));
    }

    /**
     * This is main export method. This method export opsgenie configuration to local folder.
     * If git is enabled from BackupProperties parameters it will export those configurations to remote git.
     *
     * @throws GitAPIException
     */

    public void export() throws GitAPIException {
        if (getBackupProperties().isGitEnabled()) {
            cloneGit(getBackupProperties());
        }
        init();
        logger.info("Export operation started!");
        for (ExporterInterface exporter : exporters) {
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
