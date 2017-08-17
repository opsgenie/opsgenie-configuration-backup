package com.opsgenie.tools.backup;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BackupUtils {

    private static final Logger logger = LogManager.getLogger(BackupUtils.class);

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

    public static boolean deleteDirectory(File dir) {
        if (!dir.exists()) {
            return false;
        } else if (!dir.isDirectory()) {
            return dir.delete();
        }
        String[] files = dir.list();
        for (int i = 0, len = files.length; i < len; i++) {
            File f = new File(dir, files[i]);
            if (f.isDirectory()) {
                deleteDirectory(f);
            } else {
                f.delete();
            }
        }
        return dir.delete();
    }

    public static String toJson(Object object) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        return mapper.writeValueAsString(object);
    }

    public static void fromJson(Object object, String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        mapper.readerForUpdating(object).readValue(json);
        mapper.setDateFormat(sdf);
    }

    public static List<String> findIntegrationDirectories(String path) {
        List<String> files = new ArrayList<String>();
        File[] list = new File(path).listFiles();
        if (list == null) return Collections.emptyList();
        for (File integrationType : list) {
            if (integrationType.isDirectory()) {
                for (File integrationFolder : integrationType.listFiles()) {
                    files.add(integrationFolder.getAbsolutePath());
                }
            } else {
                logger.warn(integrationType.getAbsolutePath() + " is invalid. There should be folders only in this path.");
            }
        }
        return files;
    }
}
