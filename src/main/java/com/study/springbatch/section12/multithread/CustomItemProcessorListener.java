package com.study.springbatch.section12.multithread;

import com.study.springbatch.section12.async.AsyncItem;
import com.study.springbatch.section12.async.AsyncItemDto;
import org.springframework.batch.core.ItemProcessListener;

public class CustomItemProcessorListener implements ItemProcessListener<AsyncItemDto, AsyncItem> {
    @Override
    public void beforeProcess(AsyncItemDto item) {
        ItemProcessListener.super.beforeProcess(item);
    }

    @Override
    public void afterProcess(AsyncItemDto item, AsyncItem result) {
        System.out.println("thread : " + Thread.currentThread().getName() + " process item : " + item.getId());
    }

    @Override
    public void onProcessError(AsyncItemDto item, Exception e) {
        ItemProcessListener.super.onProcessError(item, e);
    }
}
