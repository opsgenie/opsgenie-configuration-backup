package com.opsgenie.tools.backup.retry;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(value = Include.NON_NULL)
public class RateLimitsDto {

    @JsonProperty("rateLimits")
    private List<RateLimitDto> rateLimits;

    public List<RateLimitDto> getRateLimits() {
        return rateLimits;
    }

    public RateLimitsDto setRateLimits(List<RateLimitDto> rateLimits) {
        this.rateLimits = rateLimits;
        return this;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("RateLimitsDto{");
        sb.append("rateLimits=").append(rateLimits);
        sb.append('}');
        return sb.toString();
    }
}
