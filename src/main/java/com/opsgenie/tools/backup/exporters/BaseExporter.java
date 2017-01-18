package com.opsgenie.tools.backup.exporters;

import com.ifountain.opsgenie.client.OpsGenieClient;
import com.ifountain.opsgenie.client.OpsGenieClientException;
import com.ifountain.opsgenie.client.model.beans.Bean;
import com.ifountain.opsgenie.client.util.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.List;

/**
 * @author Mehmet Mustafa Demir
 */
abstract class BaseExporter<T extends Bean> implements Exporter {
    private final Logger logger = LogManager.getLogger(BaseExporter.class);
    private OpsGenieClient opsGenieClient;
    private File exportDirectory;

    public BaseExporter(OpsGenieClient opsGenieClient, String backupRootDirectory, String exportDirectoryName) {
        this.opsGenieClient = opsGenieClient;
        this.exportDirectory = new File(backupRootDirectory + "/" + exportDirectoryName + "/");
        this.exportDirectory.mkdirs();
    }

    protected void exportFile(String fileName, T bean) {
        try {
            PrintWriter writer = new PrintWriter(fileName, "UTF-8");
            writer.print(JsonUtils.toJson(bean));
            writer.close();
            logger.info(getBeanFileName(bean) + " file writed.");
        } catch (Exception e) {
            logger.error("Error at writing entity, fileName=" + fileName, e);
        }
    }

    @Override
    public void export() {
        List<T> currentBeanList = null;
        try {
            currentBeanList = retrieveEntities();
        } catch (Exception e) {
            logger.error("Error at Listing " + exportDirectory.getName(), e);
        }
        if (currentBeanList != null)
            for (T bean : currentBeanList) {
                exportFile(getExportDirectory().getAbsolutePath() + "/" + getBeanFileName(bean) + ".json", bean);
            }
    }

    protected abstract String getBeanFileName(T bean);

    protected abstract List<T> retrieveEntities() throws ParseException, OpsGenieClientException, IOException;

    protected OpsGenieClient getOpsGenieClient() {
        return opsGenieClient;
    }

    protected File getExportDirectory() {
        return exportDirectory;
    }

}
