package com.opsgenie.tools.backup;


import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Common utils for export and import procedure.
 *
 * @author Mehmet Mustafa Demir
 */
public class BackupUtils {
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

    public static List<String> findIntegrationDirectories(String path) {
        List<String> files = new ArrayList<String>();
        File[] list = new File(path).listFiles();
        if (list == null) return Collections.emptyList();

        for (File integrationType : list) {
            for (File integrationFolder : integrationType.listFiles()) {
                files.add(integrationFolder.getAbsolutePath());
            }
        }
        return files;
    }
}
