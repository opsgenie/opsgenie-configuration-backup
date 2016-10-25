package com.opsgenie.tools.backup;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.Scanner;

/**
 * Common utils for export and import procedure.
 *
 * @author Mehmet Mustafa Demir
 */
public class BackupUtils {
    public static String readFileAsJson(String fileName) throws FileNotFoundException {
        Scanner scan = new Scanner(new File(fileName));
        String json = scan.nextLine();
        scan.close();
        return json;
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
}
