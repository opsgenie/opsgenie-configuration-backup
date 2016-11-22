package com.opsgenie.tools.backup;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.account.GetAccountInfoRequest;
import com.ifountain.opsgenie.client.model.account.GetAccountInfoResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.util.Properties;

/**
 * @author Mehmet Mustafa Demir
 */
public class ImportMain {
    private final static Logger logger = LogManager.getLogger(ImportMain.class);

    public static void main(String[] args) throws IOException, GitAPIException, ParseException, OpsGenieClientException {
        logger.info("OpsGenieRestoreExecutable is started!");

        String path = System.getProperty("user.dir");
        logger.info("Running path is = " + path);
        logger.info("Argument number is = " + args.length);

        for (int i = 0; i < args.length; i++) {
            logger.info(i + ". argument is = " + args[i]);
        }
        logger.info("Possible run configurations are:\n" +
                "java -jar OpsGenieRestoreExecutable apiKey\n" +
                "java -jar OpsGenieRestoreExecutable apiKey OpsGenieBackupsHomePath\n" +
                "java -jar OpsGenieRestoreExecutable apiKey gitSSHURI SSHKeyPath\n" +
                "java -jar OpsGenieRestoreExecutable apiKey gitSSHURI SSHKeyPath OpsGenieBackupsHomePath\n");

        String backupFolderHomePath = null;
        String gitSSHURI = null;
        String sshKeyPath = null;
        String passphrase = null;
        String apiKey = null;

        if (args.length == 1) {
            logger.info("Your run configuration is:\njava -jar OpsGenieRestoreExecutable apiKey");
            apiKey = args[0];
        } else if (args.length == 2) {
            logger.info("Your run configuration is:\njava -jar OpsGenieRestoreExecutable apiKey OpsGenieBackupsHomePath");
            apiKey = args[0];
            backupFolderHomePath = args[1];
        } else if (args.length == 3) {
            logger.info("Your run configuration is:\njava -jar OpsGenieRestoreExecutable apiKey gitSSHURI SSHKeyPath");
            apiKey = args[0];
            gitSSHURI = args[1];
            sshKeyPath = args[2];
        } else if (args.length == 4) {
            logger.info("Your run configuration is:\njava -jar OpsGenieRestoreExecutable apiKey gitSSHURI SSHKeyPath OpsGenieBackupsHomePath");
            apiKey = args[0];
            gitSSHURI = args[1];
            sshKeyPath = args[2];
            backupFolderHomePath = args[3];
        } else if (args.length == 5) {
            logger.info("Your run configuration is:\njava -jar OpsGenieBackupExecutable apiKey gitSSHURI SSHKeyPath sshPassphrase extractPath");
            apiKey = args[0];
            gitSSHURI = args[1];
            sshKeyPath = args[2];
            passphrase = args[3];
            backupFolderHomePath = args[4];
        } else {
            logger.error("Your paramater number " + args.length + " is invalid.");
            logger.error("Please execute with a valid configuration.");
            throw new RuntimeException("Invalid parameter number!");
        }


        GetAccountInfoRequest getAccountInfoRequest = new GetAccountInfoRequest();
        OpsGenieClient opsGenieClient = new OpsGenieClient();
        opsGenieClient.setApiKey(apiKey);

        GetAccountInfoResponse response = opsGenieClient.account().getAccount(getAccountInfoRequest);
        logger.info("Account name is " + response.getAccount().getName());


        BackupProperties properties = new BackupProperties();

        logger.info("The api Key is = " + apiKey);
        properties.setApiKey(apiKey);

        if (backupFolderHomePath != null) {
            path = backupFolderHomePath;
        }

        logger.info("The OpsGenieBackups folder Home Path path is = " + path);
        properties.setPath(path);

        if (gitSSHURI != null && sshKeyPath != null) {
            properties.setGitEnabled(true);
            properties.setGitSshUri(gitSSHURI);
            properties.setSshKeyPath(sshKeyPath);
            properties.setPassphrase(passphrase);
            logger.info("Restore from git is enabled.");
            logger.info("The git SSH URI = " + gitSSHURI);
            logger.info("The SSH key path = " + sshKeyPath);
            if (passphrase != null) {
                logger.info("The SSH key passphrase length = " + passphrase.length());
            }
        }
        ImportConfig config = extractRestoreConfig();
        Importer importer = null;
        if (config != null) {
            importer = new Importer(properties, config);
        } else {
            importer = new Importer(properties);
        }
        importer.restore();

        logger.info("End");

    }

    public static ImportConfig extractRestoreConfig() {
        File configFile = new File("restoreConfig.properties");
        Properties props = null;
        if (configFile.exists()) {
            try {
                ImportConfig config = new ImportConfig();
                props = new Properties();
                FileReader reader = new FileReader(configFile);
                props.load(reader);
                reader.close();

                String str = props.getProperty("addNewHeartbeats");
                if (str != null && str.contains("false")) {
                    config.setAddNewHeartbeats(false);
                    logger.warn("Adding new heartbeats disabled.");
                }
                str = props.getProperty("updateExistHeartbeats");
                if (str != null && str.contains("false")) {
                    config.setUpdateExistHeartbeats(false);
                    logger.warn("Updating existing heartbeats disabled.");
                }

                str = props.getProperty("addNewUsers");
                if (str != null && str.contains("false")) {
                    config.setAddNewUsers(false);
                    logger.warn("Adding new users disabled.");
                }
                str = props.getProperty("updateExistUsers");
                if (str != null && str.contains("false")) {
                    config.setUpdateExistUsers(false);
                    logger.warn("Updating existing users disabled.");
                }

                str = props.getProperty("addNewGroups");
                if (str != null && str.contains("false")) {
                    config.setAddNewGroups(false);
                    logger.warn("Adding new groups disabled.");
                }
                str = props.getProperty("updateExistGroups");
                if (str != null && str.contains("false")) {
                    config.setUpdateExistGroups(false);
                    logger.warn("Updating existing groups disabled.");
                }

                str = props.getProperty("addNewTeams");
                if (str != null && str.contains("false")) {
                    config.setAddNewTeams(false);
                    logger.warn("Adding new teams disabled.");
                }
                str = props.getProperty("updateExistTeams");
                if (str != null && str.contains("false")) {
                    config.setUpdateExistTeams(false);
                    logger.warn("Updating existing teams disabled.");
                }

                str = props.getProperty("addNewSchedules");
                if (str != null && str.contains("false")) {
                    config.setAddNewSchedules(false);
                    logger.warn("Adding new schedules disabled.");
                }
                str = props.getProperty("updateExistSchedules");
                if (str != null && str.contains("false")) {
                    config.setUpdateExistSchedules(false);
                    logger.warn("Updating existing schedules disabled.");
                }

                str = props.getProperty("addNewEscalations");
                if (str != null && str.contains("false")) {
                    config.setAddNewEscalations(false);
                    logger.warn("Adding new escalations disabled.");
                }
                str = props.getProperty("updateExistEscalations");
                if (str != null && str.contains("false")) {
                    config.setUpdateExistEscalations(false);
                    logger.warn("Updating existing escalations disabled.");
                }

                str = props.getProperty("addNewNotifications");
                if (str != null && str.contains("false")) {
                    config.setAddNewNotifications(false);
                    logger.warn("Adding new notifications disabled.");
                }
                str = props.getProperty("updateExistNotifications");
                if (str != null && str.contains("false")) {
                    config.setUpdateExistNotifications(false);
                    logger.warn("Updating existing notifications disabled.");
                }

                str = props.getProperty("addNewTeamRoutingRules");
                if (str != null && str.contains("false")) {
                    config.setAddNewTeamRoutingRules(false);
                    logger.warn("Adding new team routing rules disabled.");
                }
                str = props.getProperty("updateExistTeamRoutingRules");
                if (str != null && str.contains("false")) {
                    config.setUpdateExistTeamRoutingRules(false);
                    logger.warn("Updating existing team routing rules  disabled.");
                }

                str = props.getProperty("addNewUserForwarding");
                if (str != null && str.contains("false")) {
                    config.setAddNewUserForwarding(false);
                    logger.warn("Adding new user forwarding disabled.");
                }
                str = props.getProperty("updateExistUserForwarding");
                if (str != null && str.contains("false")) {
                    config.setUpdateExistUserForwarding(false);
                    logger.warn("Updating existing user forwarding disabled.");
                }

                str = props.getProperty("addNewScheduleOverrides");
                if (str != null && str.contains("false")) {
                    config.setAddNewScheduleOverrides(false);
                    logger.warn("Adding new schedule overrides disabled.");
                }
                str = props.getProperty("updateExistScheduleOverrides");
                if (str != null && str.contains("false")) {
                    config.setUpdateExistScheduleOverrides(false);
                    logger.warn("Updating existing schedule overrides disabled.");
                }

                return config;

            } catch (Exception e) {
                logger.error("Error at reading restoreConfig.properties file", e);
            }
        } else {
            logger.warn("restoreConfig.properties file cannot be found.");
        }
        logger.warn("Default Import configs will be used.");
        return null;
    }
}
