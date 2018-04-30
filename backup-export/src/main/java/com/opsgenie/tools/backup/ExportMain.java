package com.opsgenie.tools.backup;

import com.beust.jcommander.JCommander;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opsgenie.oas.sdk.ApiClient;
import com.opsgenie.oas.sdk.Configuration;
import com.opsgenie.oas.sdk.api.AccountApi;
import com.opsgenie.oas.sdk.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExportMain {
    private final static Logger logger = LoggerFactory.getLogger(ExportMain.class);

    public static void main(String[] args) throws Exception {
        CommandLineArgs commandLineArgs = new CommandLineArgs();
        final JCommander argumentParser = new JCommander(commandLineArgs);
        argumentParser.setProgramName("OpsGenieConfigExporter");
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

        logger.info("Export directory path: " + backupPath);
        properties.setPath(backupPath);

        properties.setApiKey(apiKey);

        logger.info("Opsgenie host: " + opsGenieHost);
        properties.setOpsgenieUrl(opsGenieHost);


        if (sshUrl != null && sshKeyPath != null) {
            logger.info("Export the git is enabled.");
            properties.setGitEnabled(true);

            logger.info("The git SSH URI: " + sshUrl);
            properties.setGitSshUri(sshUrl);

            logger.info("The SSH key path: " + sshKeyPath);
            properties.setSshKeyPath(sshKeyPath);

            properties.setPassphrase(sshPassphrase);
        }

        configureDefaultApiClient(apiKey, opsGenieHost, debug);

        AccountApi accountApi = new AccountApi();
        try {
            final GetAccountInfoResponse info = accountApi.getInfo();
            logger.info("Account name is " + info.getData().getName() + "\n");

            ConfigurationExporter exporter = new ConfigurationExporter(properties);
            exporter.export();

            logger.info("Finished");
        }
        catch (Exception e)
        {
            logger.error("Could not connect to host: " + opsGenieHost);
            System.exit(1);
        }
    }

    private static void configureDefaultApiClient(String apiKey, String opsGenieHost, boolean debug) {
        final ApiClient defaultApiClient = Configuration.getDefaultApiClient();
        defaultApiClient.setApiKeyPrefix("GenieKey");
        defaultApiClient.setApiKey(apiKey);
        defaultApiClient.setBasePath(opsGenieHost);
        defaultApiClient.setDebugging(debug);

        ObjectMapper mapper = defaultApiClient.getJSON().getContext(Object.class);
        mapper.addMixIn(Recipient.class, IgnoredIdAndType.class);
        mapper.addMixIn(Filter.class, IgnoredType.class);
        mapper.addMixIn(TimeRestrictionInterval.class, IgnoredType.class);
        mapper.addMixIn(DeprecatedAlertPolicy.class, IgnoredType.class);
        mapper.addMixIn(Integration.class, IgnoredType.class);
        mapper.addMixIn(BaseIntegrationAction.class, IgnoredIdAndType.class);
    }

    abstract class IgnoredIdAndType {
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        String id;

        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        String type;
    }

    abstract class IgnoredType {
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        String type;
    }

}
