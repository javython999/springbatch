package com.study.springbatch.section13.skipretry;

public class CustomRetryException extends Exception {
    public CustomRetryException(String message) {
        super(message);
    }
}
