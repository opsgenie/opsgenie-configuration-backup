package com.opsgenie.tools.backup.dto;

public class PolicyConfig {

    private String id;
    private String name;
    private int order;

    public int getOrder() {
        return order;
    }

    public PolicyConfig setOrder(int order) {
        this.order = order;
        return this;
    }

    public String getId() {
        return id;
    }

    public PolicyConfig setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public PolicyConfig setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PolicyConfig that = (PolicyConfig) o;

        if (order != that.order) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return name != null ? name.equals(that.name) : that.name == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + order;
        return result;
    }
}
