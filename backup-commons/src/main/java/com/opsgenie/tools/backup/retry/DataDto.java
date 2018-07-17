package com.opsgenie.tools.backup.retry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(value = Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataDto {

    @JsonProperty("data")
    private RateLimitsDto data;

    public RateLimitsDto getData() {
        return data;
    }

    public DataDto setData(RateLimitsDto data) {
        this.data = data;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DataDto dataDto = (DataDto) o;

        return data != null ? data.equals(dataDto.data) : dataDto.data == null;
    }

    @Override
    public int hashCode() {
        return data != null ? data.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("DataDto{");
        sb.append("data=").append(data);
        sb.append('}');
        return sb.toString();
    }
}