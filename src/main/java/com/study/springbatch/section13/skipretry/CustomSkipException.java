package com.study.springbatch.section13.skipretry;

public class CustomSkipException extends Exception {

    public CustomSkipException() {super();}

    public CustomSkipException(String message) {super(message);}
}
