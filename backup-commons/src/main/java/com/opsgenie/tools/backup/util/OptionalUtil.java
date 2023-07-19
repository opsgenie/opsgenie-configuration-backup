package com.opsgenie.tools.backup.util;

public class OptionalUtil<T> {
    private final T value;
    private static final OptionalUtil<?> EMPTY = new OptionalUtil();

    private OptionalUtil() {
        this.value = null;
    }

    private OptionalUtil(T value) {
        this.value = value;
    }

    public static<T> OptionalUtil<T> empty() {
        return  (OptionalUtil<T>) EMPTY;
    }

    public static <T> OptionalUtil<T> of(T value) {
        if (value == null) {
            throw new NullPointerException("Value cannot be null");
        }
        return new OptionalUtil<T>(value);
    }

    public T get() {
        if (value == null) {
            throw new IllegalStateException("Value is not present");
        }
        return value;
    }

    public boolean isPresent() {
        return value != null;
    }

    public T orElse(T defaultValue) {
        return value != null ? value : defaultValue;
    }
}
