package com.opsgenie.tools.backup.retry;

import com.opsgenie.oas.sdk.ApiException;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

public class RetryPolicyAdapter {
    private static final Logger logger = LoggerFactory.getLogger(RetryPolicyAdapter.class);
    private static final int RATE_LIMITING_STATUS_CODE = 429;
    private static final int DEFAULT_MAX_RETRIES = 20;
    private static AtomicInteger configurationRetryCount = new AtomicInteger();
    private static AtomicInteger configurationApiCalls = new AtomicInteger();
    private static AtomicInteger searchRetryCount = new AtomicInteger();
    private static AtomicInteger searchApiCalls = new AtomicInteger();
    private static long startTime;
    private static int configLimitInMin = 0;
    private static int searchLimitInMin = 0;
    private static boolean initialized = false;

    public static void init(RateLimitManager rateLimitManager) {
        configLimitInMin = rateLimitManager.getRateLimit(DomainNames.CONFIGURATION, 60);
        searchLimitInMin = rateLimitManager.getRateLimit(DomainNames.SEARCH, 60);
        startTime = System.currentTimeMillis();
        initialized = true;
    }

    public static <T> T invoke(Callable<T> method, DomainNames domain) throws Exception {
        if (!initialized) {
            throw new IllegalStateException("RetryPolicyAdapter should be initialized before invoking api calls");
        }
        AtomicInteger apiRequestCounter = getCounterForDomain(domain);
        AtomicInteger retryCountForDomain = getRetryCountForDomain(domain);
        while (retryCountForDomain.get() < DEFAULT_MAX_RETRIES) {
            try {
                sleepIfLimitIsExceeded(domain);
                T result = method.call();
                retryCountForDomain.getAndSet(0);
                return result;
            } catch (ApiException e) {
                if (isRateLimited(e) || isInternalServerError(e)) {
                    sleepAndIncrementCounter(domain);
                    logger.info("Retrying, # of retries: " + retryCountForDomain + "  error code:" + e.getCode());
                } else {
                    throw e;
                }
            }
        }
        throw new Exception("Max number of api call tries exceeded");
    }

    public static <T> T invoke(Callable<T> method) throws Exception {
        return invoke(method, DomainNames.CONFIGURATION);
    }

    private static boolean isInternalServerError(ApiException e) {
        return e.getCode() >= HttpStatus.SC_INTERNAL_SERVER_ERROR ;
    }

    private static boolean isRateLimited(ApiException e) {
        return e.getCode() == RATE_LIMITING_STATUS_CODE;
    }


    private static void sleepAndIncrementCounter(DomainNames domain) throws InterruptedException {
        AtomicInteger retryCount = getRetryCountForDomain(domain);
        long sleepDuration = 200 * (long) Math.pow(2, retryCount.get());
        logger.info("sleeping for " + sleepDuration + " milliseconds.");
        Thread.sleep(sleepDuration);
        retryCount.incrementAndGet();
    }

    private static AtomicInteger getRetryCountForDomain(DomainNames domain) {
        return domain == DomainNames.SEARCH ? searchRetryCount : configurationRetryCount;
    }

    private static AtomicInteger getCounterForDomain(DomainNames domain) {
        return domain == DomainNames.SEARCH ? searchApiCalls : configurationApiCalls;
    }
    private static int getLimitForDomain(DomainNames domain){
        return domain == DomainNames.SEARCH ? searchLimitInMin : configLimitInMin;
    }

    private static void sleepIfLimitIsExceeded(DomainNames domain) throws InterruptedException {
        AtomicInteger apiCallCount = getCounterForDomain(domain);
        if ((apiCallCount.get()) >= (getLimitForDomain(domain))) {
            long current = System.currentTimeMillis();
            int sleepTime = 60000 - (int) (current - startTime);
            startTime = current;
            apiCallCount.getAndSet(0);
            logger.warn("thread will sleep for :" + sleepTime + " milliseconds.");
            Thread.sleep(sleepTime);
        }
    }
}

