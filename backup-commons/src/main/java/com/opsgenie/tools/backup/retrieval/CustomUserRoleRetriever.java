package com.opsgenie.tools.backup.retrieval;

import com.opsgenie.oas.sdk.api.CustomUserRoleApi;
import com.opsgenie.oas.sdk.model.CustomUserRole;
import com.opsgenie.oas.sdk.model.GetCustomUserRoleRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CustomUserRoleRetriever implements EntityRetriever<CustomUserRole>{

    private static final Logger logger = LoggerFactory.getLogger(CustomUserRoleRetriever.class);

    private static final CustomUserRoleApi customUserRoleApi = new CustomUserRoleApi();

    @Override
    public List<CustomUserRole> retrieveEntities() {
        logger.info("------------------------------------");
        logger.info("Retrieving current custom user role configurations");
        List<CustomUserRole> roleSummaryList = customUserRoleApi.listCustomUserRoles().getData();
        List<CustomUserRole> customUserRoleList = new ArrayList<CustomUserRole>();
        for (CustomUserRole role: roleSummaryList) {
            GetCustomUserRoleRequest request = new GetCustomUserRoleRequest();
            request.setIdentifier(role.getId());
            request.setIdentifierType(GetCustomUserRoleRequest.IdentifierTypeEnum.ID);
            customUserRoleList.add(customUserRoleApi.getCustomUserRole(request).getData());
        }
        return customUserRoleList;
    }
}
