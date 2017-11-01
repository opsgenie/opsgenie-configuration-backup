package com.opsgenie.tools.backup.retrieval;

import java.util.List;

public interface EntityRetriever<T> {

    List<T> retrieveEntities() throws Exception;
}
