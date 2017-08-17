package com.opsgenie.tools.backup.importers;

import com.opsgenie.client.ApiException;
import com.opsgenie.tools.backup.RestoreException;

/**
 * Interface for import classes
 *
 * @author Mehmet Mustafa Demir
 */
public interface Importer {
    void restore() throws RestoreException, ApiException;
}
