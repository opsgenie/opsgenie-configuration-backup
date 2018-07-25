package com.opsgenie.tools.backup.retry;

import com.opsgenie.oas.sdk.ApiException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class RetryMechanism {

    private static final Logger logger = LoggerFactory.getLogger(RetryMechanism.class);

    private static final int DEFAULT_MAX_RETRIES = 2;

    private int maxRetries = DEFAULT_MAX_RETRIES;

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Around("execution(* com.opsgenie.oas.sdk.ApiClient.invokeAPI(..))")
    public Object doConcurrentOperation(ProceedingJoinPoint pjp) throws Throwable {
        int numAttempts = 0;
        while (numAttempts < maxRetries) {
            numAttempts++;
            try {
                return pjp.proceed();
            } catch (ApiException ex) {
                if (ex.getCode() == 429) {
                    logger.warn("Rate limiting reached, waiting 60 seconds before retrying.");
                } else {
                    throw ex;
                }
            }
        }
        throw new Exception("Api request still not successful after " + maxRetries + " retries. Aborting");
    }
}
