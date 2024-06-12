package com.study.springbatch.section13.chunkitem;

import org.springframework.batch.core.ItemReadListener;

public class CustomItemReadListener implements ItemReadListener {

    @Override
    public void beforeRead() {
        System.out.println(">> before Read");
    }

    @Override
    public void afterRead(Object item) {
        System.out.println(">> after Read");
    }

    @Override
    public void onReadError(Exception ex) {
        System.out.println(">> onReadError");
    }
}
