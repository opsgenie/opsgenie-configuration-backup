package com.opsgenie.tools.backup.retrieval;

import com.opsgenie.oas.sdk.ApiException;
import jdk.nashorn.internal.objects.Global;

import javax.ws.rs.core.GenericType;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class apiAdapter {

    private static final int DEFAULT_MAX_RETRIES = 20;

    public static int amountOfRetries = 0;
    public static AtomicInteger noOfApiCalls = new AtomicInteger();

    public static  <T> T invoke(Callable<T> method) throws Exception {
        while(amountOfRetries<DEFAULT_MAX_RETRIES){
            try{
                System.out.println("# of api calls :" + noOfApiCalls.getAndIncrement() );
                return method.call();
            }catch (Exception e){
                if(e instanceof ApiException){
                    ApiException apiException = (ApiException) e;
                    int exceptionCode = apiException.getCode();
                    if(exceptionCode==429){
                        amountOfRetries++;
                    }else if(exceptionCode<=500){
                        amountOfRetries++;
                    }
                }
                throw e;
            }
        }
        throw new Exception("Max number of api call tries exceeded");


    }
}

