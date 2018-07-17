package com.opsgenie.tools.backup.retry;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(value = Include.NON_NULL)
public class DomainLimitDto {

    @JsonProperty("limit")
    private int limit;

    @JsonProperty("periodInSeconds")
    private int periodInSeconds;

    public int getLimit() {
        return limit;
    }

    public DomainLimitDto setLimit(int limit) {
        this.limit = limit;
        return this;
    }

    public int getPeriodInSeconds() {
        return periodInSeconds;
    }

    public DomainLimitDto setPeriodInSeconds(int periodInSeconds) {
        this.periodInSeconds = periodInSeconds;
        return this;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DomainLimitDto{");
        sb.append("limit=").append(limit);
        sb.append(", periodInSeconds=").append(periodInSeconds);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DomainLimitDto that = (DomainLimitDto) o;

        if (limit != that.limit) return false;
        return periodInSeconds == that.periodInSeconds;
    }

    @Override
    public int hashCode() {
        int result = limit;
        result = 31 * result + periodInSeconds;
        return result;
    }
}
