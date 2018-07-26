package com.opsgenie.tools.backup.retry;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(value = Include.NON_NULL)
public class RateLimitDto {

    @JsonProperty("domain")
    private String domain;

    @JsonProperty("limits")
    private List<DomainLimitDto> limits;

    public String getDomain() {
        return domain;
    }

    public RateLimitDto setDomain(String domain) {
        this.domain = domain;
        return this;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RateLimitDto{");
        sb.append("domain='").append(domain).append('\'');
        sb.append(", limits=").append(limits);
        sb.append('}');
        return sb.toString();
    }

    public List<DomainLimitDto> getLimits() {
        return limits;
    }

    public RateLimitDto setLimits(List<DomainLimitDto> limits) {
        this.limits = limits;
        return this;
    }

}

