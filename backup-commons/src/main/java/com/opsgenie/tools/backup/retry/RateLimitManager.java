package com.opsgenie.tools.backup.retry;

import java.util.ArrayList;
import java.util.List;

public class RateLimitManager {

    private RateLimitsDto rateLimitsDto;

    public RateLimitManager(RateLimitsDto rateLimitsDto) {
        this.rateLimitsDto = rateLimitsDto;
    }

    private int getApiLimitForDomain(DomainNames domain, int periodInSeconds) {
        List<DomainLimitDto> domainDtoList = new ArrayList<DomainLimitDto>();

        List<RateLimitDto> rateLimitDtoList = rateLimitsDto.getRateLimits();
        for (RateLimitDto rateLimitDto : rateLimitDtoList) {
            if (rateLimitDto.getDomain().equals(domain.value())) {
                domainDtoList = rateLimitDto.getLimits();
            }
        }
        int resultLimit = 1;
        for (DomainLimitDto domainLimitDto : domainDtoList) {
            if (domainLimitDto.getPeriodInSeconds() == periodInSeconds) {
                resultLimit = domainLimitDto.getLimit();
            }
        }
        return resultLimit;
    }

    public int getRateLimit(DomainNames domain, int period) {
        if (rateLimitsDto != null) {
            return getApiLimitForDomain(domain, period);
        }
        return period;
    }
}
