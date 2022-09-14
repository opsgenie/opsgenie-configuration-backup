package com.opsgenie.tools.backup.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.opsgenie.tools.backup.dto.PolicyConfig;
import com.opsgenie.tools.backup.retry.DataDto;
import com.opsgenie.tools.backup.retry.RateLimitsDto;
import org.apache.http.HttpHeaders;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class BackupUtils {

    private static final Logger logger = LoggerFactory.getLogger(BackupUtils.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new JodaModule());
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm"));
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static String readFile(String fileName) throws IOException {
        InputStream is = new FileInputStream(fileName);
        BufferedReader buf = new BufferedReader(new InputStreamReader(is));

        String line = buf.readLine();
        StringBuilder sb = new StringBuilder();

        while (line != null) {
            sb.append(line).append("\n");
            line = buf.readLine();
        }

        return sb.toString();
    }

    public static RateLimitsDto generateRateLimits(String apiKey, String opsGenieHost) throws IOException {
        try {
            HttpClient client = HttpClientBuilder.create().useSystemProperties().build();

            HttpGet httpGet = new HttpGet(opsGenieHost + "/v2/request-limits/");
            httpGet.addHeader(HttpHeaders.AUTHORIZATION, "GenieKey " + apiKey);

            String body = client.execute(httpGet, new BasicResponseHandler());
            DataDto result = new DataDto();
            BackupUtils.fromJson(result, body);

            return result.getData();
        } catch (Exception e) {
            logger.error("Could not initiate rate limits. " + e.getMessage());
            System.exit(1);
            return null;
        }
    }

    public static String[] getFileListOf(File directory) {
        if (directory != null && directory.exists() && directory.isDirectory()) {
            return directory.list(new FilenameFilter() {
                public boolean accept(File file, String s) {
                    return s.contains(".") && s.substring(s.lastIndexOf(".")).equals(".json");
                }
            });
        }
        return null;
    }

    public static boolean checkValidString(String s) {
        return (s != null && s.trim().length() > 0);
    }

    public static boolean isEmptyString(String s) {
        return (s == null || s.trim().length() == 0);
    }

    public static void deleteDirectory(File dir) {
        if (!dir.exists()) {
            return;
        } else if (!dir.isDirectory()) {
            dir.delete();
            return;
        }
        String[] files = dir.list();
        for (String file : files) {
            File f = new File(dir, file);
            if (f.isDirectory()) {
                deleteDirectory(f);
            } else {
                f.delete();
            }
        }
        dir.delete();
    }

    public static String toJson(Object object) throws JsonProcessingException {
        return mapper.writeValueAsString(object);
    }

    public static void fromJson(Object object, String json) throws IOException {
        mapper.readerForUpdating(object).readValue(json);
    }

    public static List<PolicyConfig> readWithTypeReference(String json) throws IOException {
        return mapper.readValue(json, new TypeReference<List<PolicyConfig>>() {
        });
    }
}
