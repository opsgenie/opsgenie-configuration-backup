package com.opsgenie.tools.backup.api;

import com.ifountain.opsgenie.client.util.JsonUtils;
import com.opsgenie.apache.http.HttpEntity;
import com.opsgenie.apache.http.HttpEntityEnclosingRequest;
import com.opsgenie.apache.http.HttpResponse;
import com.opsgenie.apache.http.HttpStatus;
import com.opsgenie.apache.http.client.HttpClient;
import com.opsgenie.apache.http.client.methods.HttpGet;
import com.opsgenie.apache.http.client.methods.HttpPost;
import com.opsgenie.apache.http.client.methods.HttpPut;
import com.opsgenie.apache.http.client.methods.HttpUriRequest;
import com.opsgenie.apache.http.entity.StringEntity;
import com.opsgenie.apache.http.impl.client.DefaultHttpClient;
import com.opsgenie.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * @author Mehmet Baris Kalkar
 * @version 12/21/16
 */
public class IntegrationApiRequester {

    private final Logger logger = LogManager.getLogger(IntegrationApiRequester.class);

    private HttpClient client;
    private String apiKey;

    private static final String INTEGRATION_API_URI = "/v2/integrations/";
    private static final String TARGET_HOST = "http://api.opsgenie.com";

    private static int retryCount = 1;
    private static final int MAX_RETRY_COUNT = 5;

    private static final int TOO_MANY_REQUESTS = 429;
    private static final int SERVICE_UNAVAILABLE = 503;
    private static final int UNAUTHENTICATED = 401;
    private static final int DEPRECATED = 410;
    private static final int FORBIDDEN = 403;
    private static final int UNAUTHORIZED_INTERNAL = 40301;


    public IntegrationApiRequester(String apiKey) {
        this.apiKey = apiKey;
        this.client = new DefaultHttpClient();
    }

    public Map<String, Object> getIntegration(String id) throws Exception {
        final HttpGet request = new HttpGet(TARGET_HOST + INTEGRATION_API_URI + id);
        setRequestAuthorizationHeader(request);
        final Map<String, Object> response = executeRequest(request);
        return (Map<String, Object>) response.get("data");
    }

    private void setRequestAuthorizationHeader(HttpUriRequest request) {
        request.setHeader("Authorization", "GenieKey " + apiKey);
    }

    private Map<String, Object> executeRequest(HttpUriRequest request) throws Exception {
        final HttpResponse httpResponse = client.execute(request);
        final int responseCode = httpResponse.getStatusLine().getStatusCode();
        if (isSuccessful(responseCode)) {
            return convertEntityToMap(httpResponse.getEntity());
        } else {
            EntityUtils.consume(httpResponse.getEntity());
            if (responseCode == TOO_MANY_REQUESTS || responseCode == SERVICE_UNAVAILABLE) {
                return retryRequest(request);
            } else if (responseCode == UNAUTHENTICATED) {
                throw new UnauthenticatedApiRequestException(httpResponse.getStatusLine().toString());
            } else if (responseCode == DEPRECATED) {
                throw new DeprecatedApiRequestException(httpResponse.getStatusLine().toString());
            } else if (responseCode == FORBIDDEN) {
                return handleUnauthorizedException(httpResponse);
            } else {
                throw new Exception(httpResponse.getStatusLine().toString());
            }
        }
    }

    private boolean isSuccessful(int statusCode) {
        return statusCode == HttpStatus.SC_OK || statusCode == HttpStatus.SC_CREATED;
    }

    private Map<String, Object> convertEntityToMap(HttpEntity httpEntity) throws IOException {
        final Scanner scanner = new Scanner(httpEntity.getContent(), "UTF-8").useDelimiter("\\A");
        String json = "{}";
        if (scanner.hasNext()) {
            json = scanner.next();
        }
        return (Map<String, Object>) JsonUtils.parse(json);
    }

    private Map<String, Object> retryRequest(HttpUriRequest request) throws Exception {
        if (retryCount > MAX_RETRY_COUNT) {
            throw new MaxRetryCountExceededException("Stopped retrying after 5 times.");
        }
        logger.info("Too many requests. Waiting for " + (int) Math.pow(2, retryCount) + " seconds before retrying");
        final double retryAfterDuration = 1000 * (Math.pow(2, retryCount++));
        Thread.sleep((long) retryAfterDuration);

        return executeRequest(request);
    }

    private Map<String, Object> handleUnauthorizedException(HttpResponse httpResponse) throws IOException, UnauthorizedApiRequestException, ForbiddenApiRequestException {
        final Map<String, Object> entity = convertEntityToMap(httpResponse.getEntity());
        if (entity.containsKey("code") && entity.get("code").equals(UNAUTHORIZED_INTERNAL)) {
            throw new UnauthorizedApiRequestException(httpResponse.getStatusLine().toString());
        } else {
            throw new ForbiddenApiRequestException(httpResponse.getStatusLine().toString());
        }
    }

    public List<Map<String, Object>> listIntegrations() throws Exception {
        final HttpGet request = new HttpGet(TARGET_HOST + INTEGRATION_API_URI);
        setRequestAuthorizationHeader(request);
        final Map<String, Object> response = executeRequest(request);

        return (List<Map<String, Object>>) response.get("data");
    }

    public Map<String, Object> getIntegrationActions(String id) throws Exception {
        final HttpGet request = new HttpGet(TARGET_HOST + INTEGRATION_API_URI + id + "/actions");
        setRequestAuthorizationHeader(request);

        final Map<String, Object> response = executeRequest(request);

        return (Map<String, Object>) response.get("data");
    }

    public Map<String, Object> updateIntegrationAction(Map<String, Object> integrationAction, String integrationId) throws Exception {
        final HttpPut request = new HttpPut(TARGET_HOST + INTEGRATION_API_URI + integrationId + "/actions");
        setRequestAuthorizationHeader(request);
        setRequestEntity(request, integrationAction);

        return executeRequest(request);
    }

    private void setRequestEntity(HttpEntityEnclosingRequest request, Map integration) throws IOException {
        final StringEntity params = new StringEntity(JsonUtils.toJson(integration));
        request.addHeader("content-type", "application/json");
        request.setEntity(params);
    }

    public Map<String, Object> createIntegration(Map<String, Object> integration) throws Exception {
        final HttpPost request = new HttpPost(TARGET_HOST + INTEGRATION_API_URI);
        setRequestAuthorizationHeader(request);
        setRequestEntity(request, integration);

        return executeRequest(request);
    }

    public Map<String, Object> updateIntegration(Map<String, Object> integration, String integrationId) throws Exception {
        final HttpPut request = new HttpPut(TARGET_HOST + INTEGRATION_API_URI + integrationId);
        setRequestAuthorizationHeader(request);
        setRequestEntity(request, integration);

        return executeRequest(request);
    }

}
