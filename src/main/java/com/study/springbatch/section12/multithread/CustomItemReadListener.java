package com.study.springbatch.section12.multithread;

import com.study.springbatch.section12.async.AsyncItemDto;
import org.springframework.batch.core.ItemReadListener;

public class CustomItemReadListener implements ItemReadListener<AsyncItemDto> {

    @Override
    public void beforeRead() {
        ItemReadListener.super.beforeRead();
    }

    @Override
    public void afterRead(AsyncItemDto item) {
        System.out.println("thread : " + Thread.currentThread().getName() + " read item : " + item.getId());
    }

    @Override
    public void onReadError(Exception ex) {
        ItemReadListener.super.onReadError(ex);
    }
}
