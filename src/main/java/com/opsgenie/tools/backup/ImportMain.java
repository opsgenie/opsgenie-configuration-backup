package com.opsgenie.tools.backup;

import com.beust.jcommander.JCommander;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opsgenie.oas.sdk.ApiClient;
import com.opsgenie.oas.sdk.Configuration;
import com.opsgenie.oas.sdk.api.AccountApi;
import com.opsgenie.oas.sdk.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

public class ImportMain {
    private final static Logger logger = LogManager.getLogger(ImportMain.class);

    public static void main(String[] args) throws Exception {
        CommandLineArgs commandLineArgs = new CommandLineArgs();
        final JCommander argumentParser = new JCommander(commandLineArgs);
        argumentParser.setProgramName("OpsGenieConfigImporter");
        try {
            argumentParser.parse(args);
        } catch (Exception e) {
            argumentParser.usage();
            System.exit(1);
        }

        final String backupPath = commandLineArgs.getBackupPath();
        final String apiKey = commandLineArgs.getApiKey();
        final String sshUrl = commandLineArgs.getGitSshUrl();
        final String sshKeyPath = commandLineArgs.getSshKeyPath();
        final String sshPassphrase = commandLineArgs.getSshPassphrase();
        final String opsGenieHost = commandLineArgs.getOpsGenieHost();
        final boolean debug = commandLineArgs.isDebug();

        BackupProperties properties = new BackupProperties();

        logger.info("Import directory path: " + backupPath);
        properties.setPath(backupPath);

        properties.setApiKey(apiKey);

        logger.info("Opsgenie host: " + opsGenieHost);
        properties.setOpsgenieUrl(opsGenieHost);

        if (sshUrl != null && sshKeyPath != null) {
            properties.setGitEnabled(true);
            properties.setGitSshUri(sshUrl);
            properties.setSshKeyPath(sshKeyPath);
            properties.setPassphrase(sshPassphrase);
            logger.info("Restore from git is enabled.");
            logger.info("Git ssh url:" + sshUrl);
            logger.info("Ssh key path: " + sshKeyPath);
        }

        final ApiClient defaultApiClient = configureDefaultApiClient(apiKey, opsGenieHost, debug);

        configureClientObjectMapper(defaultApiClient);

        AccountApi accountApi = new AccountApi();
        final GetAccountInfoResponse info = accountApi.getInfo();
        logger.info("Account name is " + info.getData().getName() + "\n");

        ImportConfig config = extractRestoreConfig();
        ConfigurationImporter importer = new ConfigurationImporter(properties, config);
        importer.restore();
        logger.info("Finished");

    }

    private static void configureClientObjectMapper(ApiClient defaultApiClient) {
        ObjectMapper mapper = defaultApiClient.getJSON().getContext(Object.class);
        mapper.addMixIn(Filter.class, IgnoredType.class);
        mapper.addMixIn(TimeRestrictionInterval.class, IgnoredType.class);
        mapper.addMixIn(Recipient.class, IgnoredType.class);
        mapper.addMixIn(AlertPolicy.class, IgnoredType.class);
        mapper.addMixIn(Integration.class, IgnoredType.class);
        mapper.addMixIn(BaseIntegrationAction.class, IgnoredIntegration.class);
    }

    private static ApiClient configureDefaultApiClient(String apiKey, String opsGenieHost, boolean debug) {
        final ApiClient defaultApiClient = Configuration.getDefaultApiClient();
        defaultApiClient.setApiKeyPrefix("GenieKey");
        defaultApiClient.setApiKey(apiKey);
        defaultApiClient.setBasePath(opsGenieHost);
        defaultApiClient.setDebugging(debug);
        return defaultApiClient;
    }

    private static ImportConfig extractRestoreConfig() throws IOException {
        File configFile = new File("restoreConfig.properties");
        if (!configFile.exists()) {
            logger.warn("restoreConfig.properties file cannot be found.");
            logger.warn("Default Import configs will be used.");
            return null;
        } else {
            FileReader reader = null;
            try {
                ImportConfig config = new ImportConfig();
                Properties props = new Properties();
                reader = new FileReader(configFile);
                props.load(reader);

                String str = props.getProperty("addNewUsers");
                if (str != null && str.contains("false")) {
                    config.setAddNewUsers(false);
                    logger.warn("Adding new users disabled.");
                }
                str = props.getProperty("updateExistingUsers");
                if (str != null && str.contains("false")) {
                    config.setUpdateExistingUsers(false);
                    logger.warn("Updating existing users disabled.");
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

                str = props.getProperty("addNewPolicies");
                if (str != null && str.contains("false")) {
                    config.setAddNewPolicies(false);
                    logger.warn("Adding new policies disabled.");
                }

                str = props.getProperty("updateExistingPolicies");
                if (str != null && str.contains("false")) {
                    config.setUpdateExistingPolicies(false);
                    logger.warn("Updating existing policies disabled.");
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

    abstract class IgnoredType {
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String type;
    }

    abstract class IgnoredIntegration {
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String type;

        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String id;
    }

}
