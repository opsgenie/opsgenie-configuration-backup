package com.opsgenie.tools.backup;

import com.beust.jcommander.JCommander;
import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.account.GetAccountInfoRequest;
import com.ifountain.opsgenie.client.model.account.GetAccountInfoResponse;
import com.opsgenie.client.ApiClient;
import com.opsgenie.client.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.text.ParseException;

public class ExportMain {
    private final static Logger logger = LogManager.getLogger(ExportMain.class);

    public static void main(String[] args) throws IOException, GitAPIException, ParseException, OpsGenieClientException {
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

        GetAccountInfoRequest getAccountInfoRequest = new GetAccountInfoRequest();
        OpsGenieClient opsGenieClient = new OpsGenieClient();
        opsGenieClient.setApiKey(apiKey);

        final ApiClient defaultApiClient = Configuration.getDefaultApiClient();
        defaultApiClient.setApiKeyPrefix("GenieKey");
        defaultApiClient.setApiKey(apiKey);
        defaultApiClient.setBasePath(opsGenieHost);

        GetAccountInfoResponse response = opsGenieClient.account().getAccount(getAccountInfoRequest);
        logger.info("Account name is " + response.getAccount().getName() + "\n");

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
