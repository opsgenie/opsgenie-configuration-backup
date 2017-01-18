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
        System.out.println("Running path is = " + path);
        System.out.println("Argument number is = " + args.length);

        String backupFolderHomePath = null;
        String gitSSHURI = null;
        String sshKeyPath = null;
        String passphrase = null;
        String apiKey = null;

        if (args.length == 1) {
            logger.debug("Your run configuration is:\njava -jar OpsGenieRestoreExecutable apiKey");
            apiKey = args[0];
        } else if (args.length == 2) {
            logger.debug("Your run configuration is:\njava -jar OpsGenieRestoreExecutable apiKey OpsGenieBackupsHomePath");
            apiKey = args[0];
            backupFolderHomePath = args[1];
        } else if (args.length == 3) {
            logger.debug("Your run configuration is:\njava -jar OpsGenieRestoreExecutable apiKey gitSSHURI SSHKeyPath");
            apiKey = args[0];
            gitSSHURI = args[1];
            sshKeyPath = args[2];
        } else if (args.length == 4) {
            logger.debug("Your run configuration is:\njava -jar OpsGenieRestoreExecutable apiKey gitSSHURI SSHKeyPath GitClonePath");
            apiKey = args[0];
            gitSSHURI = args[1];
            sshKeyPath = args[2];
            backupFolderHomePath = args[3];
        } else if (args.length == 5) {
            logger.debug("Your run configuration is:\njava -jar OpsGenieBackupExecutable apiKey gitSSHURI SSHKeyPath sshPassphrase GitClonePath");
            apiKey = args[0];
            gitSSHURI = args[1];
            sshKeyPath = args[2];
            passphrase = args[3];
            backupFolderHomePath = args[4];
        } else {
            System.err.println("Your paramater number " + args.length + " is invalid.");
            System.err.println("Please execute with a valid configuration.");
            System.out.println("Possible run configurations are:\n" +
                    "java -jar OpsGenieRestoreExecutable apiKey\n" +
                    "java -jar OpsGenieRestoreExecutable apiKey OpsGenieBackupsHomePath\n" +
                    "java -jar OpsGenieRestoreExecutable apiKey gitSSHURI SSHKeyPath\n" +
                    "java -jar OpsGenieRestoreExecutable apiKey gitSSHURI SSHKeyPath GitClonePath\n" +
                    "java -jar OpsGenieRestoreExecutable apiKey gitSSHURI SSHKeyPath sshPassphrase GitClonePath\n");
            System.exit(1);
        }

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
            logSecretKey("SSH key passphrase", passphrase);
        }

        GetAccountInfoRequest getAccountInfoRequest = new GetAccountInfoRequest();
        OpsGenieClient opsGenieClient = new OpsGenieClient();
        opsGenieClient.setApiKey(apiKey);

        GetAccountInfoResponse response = opsGenieClient.account().getAccount(getAccountInfoRequest);
        logger.info("Account name is " + response.getAccount().getName() + "\n");

        ImportConfig config = extractRestoreConfig();
        ConfigurationImporter importer = null;
        if (config != null) {
            importer = new ConfigurationImporter(properties, config);
        } else {
            importer = new ConfigurationImporter(properties);
        }
        importer.restore();

        logger.info("Finished");

    }

    private static void logSecretKey(String propName, String secretKey) {
        if (secretKey != null) {
            String criptedKey = secretKey.substring(0, secretKey.length() / 2);
            for (int i = criptedKey.length(); i < secretKey.length(); i++) {
                criptedKey += "*";
            }
            logger.info("The " + propName + " is = " + criptedKey);
        }
    }

    public static ImportConfig extractRestoreConfig() throws IOException {
        File configFile = new File("restoreConfig.properties");
        Properties props = null;
        if (!configFile.exists()) {
            logger.warn("restoreConfig.properties file cannot be found.");
            logger.warn("Default Import configs will be used.");
            return null;
        } else {
            FileReader reader = null;
            try {
                ImportConfig config = new ImportConfig();
                props = new Properties();
                reader = new FileReader(configFile);
                props.load(reader);

                String str = props.getProperty("addNewHeartbeats");
                if (str != null && str.contains("false")) {
                    config.setAddNewHeartbeats(false);
                    logger.warn("Adding new heartbeats disabled.");
                }
                str = props.getProperty("updateExistingHeartbeats");
                if (str != null && str.contains("false")) {
                    config.setUpdateExistingHeartbeats(false);
                    logger.warn("Updating existing heartbeats disabled.");
                }

                str = props.getProperty("addNewUsers");
                if (str != null && str.contains("false")) {
                    config.setAddNewUsers(false);
                    logger.warn("Adding new users disabled.");
                }
                str = props.getProperty("updateExistingUsers");
                if (str != null && str.contains("false")) {
                    config.setUpdateExistingUsers(false);
                    logger.warn("Updating existing users disabled.");
                }

                str = props.getProperty("addNewGroups");
                if (str != null && str.contains("false")) {
                    config.setAddNewGroups(false);
                    logger.warn("Adding new groups disabled.");
                }
                str = props.getProperty("updateExistingGroups");
                if (str != null && str.contains("false")) {
                    config.setUpdateExistingGroups(false);
                    logger.warn("Updating existing groups disabled.");
                }

                str = props.getProperty("addNewTeams");
                if (str != null && str.contains("false")) {
                    config.setAddNewTeams(false);
                    logger.warn("Adding new teams disabled.");
                }
                str = props.getProperty("updateExistingTeams");
                if (str != null && str.contains("false")) {
                    config.setUpdateExistingTeams(false);
                    logger.warn("Updating existing teams disabled.");
                }

                str = props.getProperty("addNewSchedules");
                if (str != null && str.contains("false")) {
                    config.setAddNewSchedules(false);
                    logger.warn("Adding new schedules disabled.");
                }
                str = props.getProperty("updateExistingSchedules");
                if (str != null && str.contains("false")) {
                    config.setUpdateExistingSchedules(false);
                    logger.warn("Updating existing schedules disabled.");
                }

                str = props.getProperty("addNewEscalations");
                if (str != null && str.contains("false")) {
                    config.setAddNewEscalations(false);
                    logger.warn("Adding new escalations disabled.");
                }
                str = props.getProperty("updateExistingEscalations");
                if (str != null && str.contains("false")) {
                    config.setUpdateExistingEscalations(false);
                    logger.warn("Updating existing escalations disabled.");
                }

                str = props.getProperty("addNewNotifications");
                if (str != null && str.contains("false")) {
                    config.setAddNewNotifications(false);
                    logger.warn("Adding new notifications disabled.");
                }
                str = props.getProperty("updateExistingNotifications");
                if (str != null && str.contains("false")) {
                    config.setUpdateExistingNotifications(false);
                    logger.warn("Updating existing notifications disabled.");
                }

                str = props.getProperty("addNewTeamRoutingRules");
                if (str != null && str.contains("false")) {
                    config.setAddNewTeamRoutingRules(false);
                    logger.warn("Adding new team routing rules disabled.");
                }
                str = props.getProperty("updateExistingTeamRoutingRules");
                if (str != null && str.contains("false")) {
                    config.setUpdateExistingTeamRoutingRules(false);
                    logger.warn("Updating existing team routing rules  disabled.");
                }

                str = props.getProperty("addNewUserForwarding");
                if (str != null && str.contains("false")) {
                    config.setAddNewUserForwarding(false);
                    logger.warn("Adding new user forwarding disabled.");
                }
                str = props.getProperty("updateExistingUserForwarding");
                if (str != null && str.contains("false")) {
                    config.setUpdateExistingUserForwarding(false);
                    logger.warn("Updating existing user forwarding disabled.");
                }

                str = props.getProperty("addNewScheduleOverrides");
                if (str != null && str.contains("false")) {
                    config.setAddNewScheduleOverrides(false);
                    logger.warn("Adding new schedule overrides disabled.");
                }
                str = props.getProperty("updateExistingScheduleOverrides");
                if (str != null && str.contains("false")) {
                    config.setUpdateExistingScheduleOverrides(false);
                    logger.warn("Updating existing schedule overrides disabled.");
                }

                str = props.getProperty("updateExistingIntegrations");
                if (str != null && str.contains("false")) {
                    config.setUpdateExistingIntegrations(false);
                    logger.warn("Updating existing integrations disabled.");
                }

                str = props.getProperty("addNewIntegrations");
                if (str != null && str.contains("false")) {
                    config.setAddNewIntegrations(false);
                    logger.warn("Adding new integrations disabled.");
                }

                return config;

            } catch (Exception e) {
                logger.error("Error at reading restoreConfig.properties file", e);
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }
        }
        logger.warn("Default Import configs will be used.");
        return null;
    }
}
