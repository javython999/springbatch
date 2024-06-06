package com.study.springbatch.section12.multithread;

import com.study.springbatch.section12.async.AsyncItem;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;

public class CustomItemWriterListener implements ItemWriteListener<AsyncItem> {

    @Override
    public void beforeWrite(Chunk<? extends AsyncItem> items) {
        ItemWriteListener.super.beforeWrite(items);
    }

    @Override
    public void afterWrite(Chunk<? extends AsyncItem> items) {
        System.out.println("thread : " + Thread.currentThread().getName() + " write items : " + items.getItems());
    }

    @Override
    public void onWriteError(Exception exception, Chunk<? extends AsyncItem> items) {
        ItemWriteListener.super.onWriteError(exception, items);
    }
}
