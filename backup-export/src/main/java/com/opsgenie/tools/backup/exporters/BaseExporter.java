package com.opsgenie.tools.backup.exporters;

import com.opsgenie.oas.sdk.ApiException;
import com.opsgenie.tools.backup.util.BackupUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.List;

abstract class BaseExporter<T> implements Exporter {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private File exportDirectory;

    public BaseExporter(String backupRootDirectory, String exportDirectoryName) {
        this.exportDirectory = new File(backupRootDirectory + "/" + exportDirectoryName + "/");
        this.exportDirectory.mkdirs();
    }

    protected void exportFile(String fileName, T entity) {
        try {
            PrintWriter writer = new PrintWriter(fileName, "UTF-8");
            writer.print(BackupUtils.toJson(entity));
            writer.close();
            logger.info(getEntityFileName(entity) + " file written.");
        } catch (Exception e) {
            logger.error("Error at writing entity, fileName=" + fileName, e);
        }
    }

    @Override
    public void export() {
        List<T> currentBeanList;
        try {
            currentBeanList = retrieveEntities();
        } catch (Exception e) {
            logger.error("Could not list " + exportDirectory.getName(), e);
            return;
        }

        for (T bean : currentBeanList) {
            exportFile(getExportDirectory().getAbsolutePath() + "/" + getEntityFileName(bean) + ".json", bean);
        }
    }

    protected abstract String getEntityFileName(T entity);

    protected abstract List<T> retrieveEntities() throws ParseException, IOException, ApiException;

    protected File getExportDirectory() {
        return exportDirectory;
    }

}
