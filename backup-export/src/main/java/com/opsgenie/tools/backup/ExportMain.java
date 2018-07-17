package com.opsgenie.tools.backup;

import com.beust.jcommander.JCommander;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.opsgenie.oas.sdk.ApiClient;
import com.opsgenie.oas.sdk.Configuration;
import com.opsgenie.oas.sdk.JSON;
import com.opsgenie.oas.sdk.api.AccountApi;
import com.opsgenie.oas.sdk.model.*;
import com.opsgenie.tools.backup.retry.DataDto;
import com.opsgenie.tools.backup.retry.DomainLimitDto;
import com.opsgenie.tools.backup.retry.RateLimitDto;
import com.opsgenie.tools.backup.retry.RateLimitsDto;
import com.opsgenie.tools.backup.util.BackupUtils;
import jdk.nashorn.internal.parser.JSONParser;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExportMain {
    private final static Logger logger = LoggerFactory.getLogger(ExportMain.class);
    public static List<DomainLimitDto> apiSearchLimits;
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
        apiSearchLimits = getApiLimits(apiKey);
        try {
            final GetAccountInfoResponse info = accountApi.getInfo();
            logger.info("Account name is " + info.getData().getName() + "\n");

            ConfigurationExporter exporter = new ConfigurationExporter(properties);
            exporter.export();

            logger.info("Finished");
        } catch (Exception e) {
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
        mapper.addMixIn(Responder.class, IgnoredType.class);
        mapper.addMixIn(Policy.class, IgnoredType.class);
    }

    private static List<DomainLimitDto> getApiLimits(String apiKey) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet httpGet = new HttpGet("http://localhost:9004/v2/request-limits/");
        httpGet.addHeader(HttpHeaders.AUTHORIZATION, "GenieKey " + apiKey);
        HttpResponse response = client.execute(httpGet);
        ResponseHandler handler = new BasicResponseHandler();

        String body = (String) client.execute(httpGet, handler);
        Header[] headers = response.getAllHeaders();
        DataDto result = new DataDto();
        BackupUtils.fromJson(result,body);
        logger.info("****" + result);
        List<DomainLimitDto> searchLimits = new ArrayList<DomainLimitDto>();
        List<RateLimitDto> rateLimitDtoList = result.getData().getRateLimits();
        for (RateLimitDto rateLimitDto : rateLimitDtoList){
            if(rateLimitDto.getDomain().equals("search")){
                searchLimits = rateLimitDto.getLimits();
            }
        }
        return searchLimits ;
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
