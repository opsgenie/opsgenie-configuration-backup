package com.opsgenie.tools.backup.retrieval;

import com.opsgenie.oas.sdk.api.CustomUserRoleApi;
import com.opsgenie.oas.sdk.model.CustomUserRole;
import com.opsgenie.oas.sdk.model.GetCustomUserRoleRequest;
import com.opsgenie.tools.backup.retry.RetryPolicyAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;

public class CustomUserRoleRetriever implements EntityRetriever<CustomUserRole>{

    private static final Logger logger = LoggerFactory.getLogger(CustomUserRoleRetriever.class);

    private static final CustomUserRoleApi customUserRoleApi = new CustomUserRoleApi();

    @Override
    public List<CustomUserRole> retrieveEntities() throws Exception {
        logger.info("Retrieving current custom user role configurations");
        List<CustomUserRole> roleSummaryList = RetryPolicyAdapter.invoke(new Callable<List<CustomUserRole>>() {
            @Override
            public List<CustomUserRole> call()  {
                return customUserRoleApi.listCustomUserRoles().getData();
            }
        });
        List<CustomUserRole> customUserRoleList = new ArrayList<CustomUserRole>();
        for (CustomUserRole role: roleSummaryList) {
            final GetCustomUserRoleRequest request = new GetCustomUserRoleRequest();
            request.setIdentifier(role.getId());
            request.setIdentifierType(GetCustomUserRoleRequest.IdentifierTypeEnum.ID);
            customUserRoleList.add(RetryPolicyAdapter.invoke(new Callable<CustomUserRole>() {
                        @Override
                        public CustomUserRole call() {
                            CustomUserRole data = customUserRoleApi.getCustomUserRole(request).getData();
                            sortCustomUserRoleRights(data);
                            return data;
                        }
                    }));

        }
        return customUserRoleList;
    }

    private void sortCustomUserRoleRights(CustomUserRole customUserRole) {
        Collections.sort(customUserRole.getDisallowedRights(), new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
        Collections.sort(customUserRole.getGrantedRights(), new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
    }
}
