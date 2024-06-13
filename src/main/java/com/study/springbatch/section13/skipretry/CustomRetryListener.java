package com.study.springbatch.section13.skipretry;

import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;

public class CustomRetryListener implements RetryListener {

    @Override
    public <T, E extends Throwable> boolean open(RetryContext context, RetryCallback<T, E> callback) {
        System.out.println("open");
        return true;
    }

    @Override
    public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        System.out.println("close");
        RetryListener.super.close(context, callback, throwable);
    }

    @Override
    public <T, E extends Throwable> void onSuccess(RetryContext context, RetryCallback<T, E> callback, T result) {
        System.out.println("onSuccess");
        RetryListener.super.onSuccess(context, callback, result);
    }

    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        System.out.println("Retry Count : " + context.getRetryCount());
        RetryListener.super.onError(context, callback, throwable);
    }
}
