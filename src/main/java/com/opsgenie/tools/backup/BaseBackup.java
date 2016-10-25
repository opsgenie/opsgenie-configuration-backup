package com.opsgenie.tools.backup;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusLogger;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

/**
 * @author Mehmet Mustafa Demir
 */
abstract class BaseBackup {
    private final Logger logger = LogManager.getLogger(BaseBackup.class);
    private BackupProperties backupProperties;
    private Git git;
    private TransportConfigCallback callBack;

    public BaseBackup(BackupProperties backupProperties) throws FileNotFoundException, UnsupportedEncodingException, GitAPIException {
        backupProperties.validate();
        this.backupProperties = backupProperties;
        if (backupProperties.isGitEnabled()) {
            cloneGit(backupProperties);
        }
    }

    protected void cloneGit(final BackupProperties properties) throws FileNotFoundException, UnsupportedEncodingException, GitAPIException {
        properties.setPath(properties.getPath() + "/OpsgenieBackupGitRepository");
        String rootPath = properties.getPath();
        File backupGitDirectory = new File(rootPath);

        if (backupGitDirectory.exists() && (backupGitDirectory.list() != null) && (backupGitDirectory.list().length > 0)) {
            logger.warn("Destination path " + rootPath + " already exists and is not an empty directory");
            logger.warn("Destination path " + rootPath + " will be deleted inorder to clone remote git repository");
            BackupUtils.deleteDirectory(backupGitDirectory);
            backupGitDirectory.mkdirs();
        } else {
            backupGitDirectory.mkdirs();
        }

        final SshSessionFactory sshSessionFactory = new JschConfigSessionFactory() {
            @Override
            protected void configure(OpenSshConfig.Host hc, Session session) {
                session.setConfig("StrictHostKeyChecking", "no");
            }

            protected JSch createDefaultJSch(FS fs) throws JSchException {
                JSch defaultJSch = super.createDefaultJSch(fs);
                defaultJSch.removeAllIdentity();
                defaultJSch.addIdentity(properties.getSshKeyPath());
                return defaultJSch;
            }
        };

        callBack = new TransportConfigCallback() {
            public void configure(Transport transport) {
                SshTransport sshTransport = (SshTransport) transport;
                sshTransport.setSshSessionFactory(sshSessionFactory);
            }
        };

        logger.info("Cloning remote git operation started!");
        CloneCommand cloneCommand = Git.cloneRepository();
        cloneCommand.setURI(properties.getGitSshUri());
        cloneCommand.setTransportConfigCallback(callBack);
        StatusLogger.getLogger().setLevel(Level.OFF);
        cloneCommand.setDirectory(backupGitDirectory);
        git = cloneCommand.call();
        logger.info("Cloning remote git operation finished!");
    }

    protected abstract void init() throws FileNotFoundException, UnsupportedEncodingException;

    public BackupProperties getBackupProperties() {
        return backupProperties;
    }

    public BaseBackup setBackupProperties(BackupProperties backupProperties) {
        this.backupProperties = backupProperties;
        return this;
    }

    protected Git getGit() {
        return git;
    }

    protected TransportConfigCallback getCallBack() {
        return callBack;
    }
}
