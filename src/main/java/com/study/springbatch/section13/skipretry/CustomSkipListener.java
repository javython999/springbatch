package com.study.springbatch.section13.skipretry;

import org.springframework.batch.core.SkipListener;

public class CustomSkipListener implements SkipListener<Integer, String> {

    @Override
    public void onSkipInRead(Throwable t) {
        System.out.println(">> onSkipRead : " + t.getMessage());
    }

    @Override
    public void onSkipInWrite(String item, Throwable t) {
        System.out.println(">> onSkipInWrite item : " + item);
        System.out.println(">> onSkipInWrite message : " + t.getMessage());
    }

    @Override
    public void onSkipInProcess(Integer item, Throwable t) {
        System.out.println(">> onSkipInWrite item : " + item);
        System.out.println(">> onSkipInWrite message : " + t.getMessage());
    }
}
