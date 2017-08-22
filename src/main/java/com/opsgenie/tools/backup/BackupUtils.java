package com.opsgenie.tools.backup;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.text.SimpleDateFormat;

public class BackupUtils {

    private static final Logger logger = LogManager.getLogger(BackupUtils.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.registerModule(new JodaModule());
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm"));
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
        for (String file : files) {
            File f = new File(dir, file);
            if (f.isDirectory()) {
                deleteDirectory(f);
            } else {
                f.delete();
            }
        }
        return dir.delete();
    }

    public static String toJson(Object object) throws JsonProcessingException {
        return mapper.writeValueAsString(object);
    }

    public static void fromJson(Object object, String json) throws IOException {
        mapper.readerForUpdating(object).readValue(json);
    }
}
