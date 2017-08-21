package com.opsgenie.tools.backup;

import com.beust.jcommander.JCommander;
import com.opsgenie.client.ApiClient;
import com.opsgenie.client.Configuration;
import com.opsgenie.client.api.AccountApi;
import com.opsgenie.client.model.GetAccountInfoResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ExportMain {
    private final static Logger logger = LogManager.getLogger(ExportMain.class);

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

        logger.info("The api key:" + apiKey);
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

            logSecretKey("SSH key passphrase", sshPassphrase);
            properties.setPassphrase(sshPassphrase);
        }


        final ApiClient defaultApiClient = Configuration.getDefaultApiClient();
        defaultApiClient.setApiKeyPrefix("GenieKey");
        defaultApiClient.setApiKey(apiKey);
        defaultApiClient.setBasePath(opsGenieHost);
        defaultApiClient.setDebugging(debug);

        AccountApi accountApi = new AccountApi();
        final GetAccountInfoResponse info = accountApi.getInfo();
        logger.info("Account name is " + info.getData().getName() + "\n");

        ConfigurationExporter exporter = new ConfigurationExporter(properties);
        exporter.export();

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
}
