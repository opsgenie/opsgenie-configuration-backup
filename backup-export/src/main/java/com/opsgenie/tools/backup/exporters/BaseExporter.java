package com.opsgenie.tools.backup.exporters;

import com.opsgenie.tools.backup.retrieval.EntityRetriever;
import com.opsgenie.tools.backup.retry.RateLimitManager;
import com.opsgenie.tools.backup.retry.RateLimitsDto;
import com.opsgenie.tools.backup.util.BackupUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.PrintWriter;
import java.util.List;

abstract class BaseExporter<T> implements Exporter {

    final Logger logger = LoggerFactory.getLogger(getClass());
    private File exportDirectory;

    BaseExporter(String backupRootDirectory, String exportDirectoryName) {
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
        List<T> currentEntityList;
        try {
            currentEntityList = initializeEntityRetriever().retrieveEntities();
        } catch (Exception e) {
            logger.error("Could not list " + exportDirectory.getName(), e);
            return;
        }

        for (T bean : currentEntityList) {
            exportFile(getExportDirectory().getAbsolutePath() + "/" + getEntityFileName(bean) + ".json", bean);
        }
    }

    protected abstract EntityRetriever<T> initializeEntityRetriever();

    protected abstract String getEntityFileName(T entity);

    protected File getExportDirectory() {
        return exportDirectory;
    }

}
