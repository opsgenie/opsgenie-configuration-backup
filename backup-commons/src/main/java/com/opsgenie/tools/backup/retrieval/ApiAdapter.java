package com.opsgenie.tools.backup.retrieval;

import com.opsgenie.oas.sdk.ApiException;
import org.apache.http.HttpStatus;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class ApiAdapter {
    private static final int RATE_LIMITING_STATUS_CODE = 429;
    private static final int DEFAULT_MAX_RETRIES = 20;
    private static AtomicInteger configurationRetryCount = new AtomicInteger();
    private static AtomicInteger configurationApiCalls = new AtomicInteger();
    private static AtomicInteger searchRetryCount = new AtomicInteger();
    private static AtomicInteger searchApiCalls = new AtomicInteger();
    private static long startTime;
    private static int configLimitInMin = 0;

    public static <T> T invoke(Callable<T> method, String domain) throws Exception {
        AtomicInteger apiRequestCounter = getCounterForDomain(domain);
        AtomicInteger retryCountForDomain = getRetryCountForDomain(domain);
        while (retryCountForDomain.get() < DEFAULT_MAX_RETRIES) {
            try {
                initConfigLimitInMin();
                sleepIfConfLimitExceeds();
                System.out.println("# of api calls :" + apiRequestCounter.getAndIncrement());
                T result = method.call();
                retryCountForDomain.getAndSet(0);
                return result;
            } catch (ApiException e) {
                if (isRateLimited(e) || isInternalServerError(e)) {
                    sleepAndIncrementCounter(domain);
                    System.out.println("RETRYING, # of retries: " + retryCountForDomain);
                }
            }
        }
        throw new Exception("Max number of api call tries exceeded");
    }

    private static void initConfigLimitInMin() {
        if (configurationApiCalls.get() < 1) {
            configLimitInMin = UserRetriever.getSpecificApiLimit("configuration", 60);
            startTime = System.currentTimeMillis();
        }
    }

    public static <T> T invoke(Callable<T> method) throws Exception {
        return invoke(method, "configuration");
    }

    private static boolean isInternalServerError(ApiException e) {
        return e.getCode() <= HttpStatus.SC_INTERNAL_SERVER_ERROR;
    }

    private static boolean isRateLimited(ApiException e) {
        return e.getCode() == RATE_LIMITING_STATUS_CODE;
    }

    private static void sleepAndIncrementCounter(String domain) throws InterruptedException {
        AtomicInteger retryCount = getRetryCountForDomain(domain);
        long sleepDuration = 200 * (long) Math.pow(2, retryCount.get());
        System.out.println("sleeping for " + sleepDuration + " milliseconds.");
        Thread.sleep(sleepDuration);
        retryCount.incrementAndGet();
    }

    private static AtomicInteger getRetryCountForDomain(String domain) {
        return domain.equals("search") ? searchRetryCount : configurationRetryCount;
    }

    private static AtomicInteger getCounterForDomain(String domain) {
        return domain.equals("search") ? searchApiCalls : configurationApiCalls;
    }

    private static void sleepIfConfLimitExceeds() throws InterruptedException {
        if ((configurationApiCalls.get()) >= (configLimitInMin * 4 / 10)) {
            long current = System.currentTimeMillis();
            int sleepTime = 60000 - (int) (current - startTime);
            startTime = current;
            configurationApiCalls.getAndSet(0);
            System.out.println("thread will sleep for :" + sleepTime + " milliseconds.");
            Thread.sleep(sleepTime);
        }

    }
}

