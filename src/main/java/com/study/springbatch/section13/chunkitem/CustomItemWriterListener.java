package com.study.springbatch.section13.chunkitem;

import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;

public class CustomItemWriterListener implements ItemWriteListener<String> {

    @Override
    public void beforeWrite(Chunk items) {
        System.out.println(">> before Write");
    }

    @Override
    public void afterWrite(Chunk items) {
        System.out.println(">> after Write");
    }

    @Override
    public void onWriteError(Exception exception, Chunk items) {
        System.out.println(">> on Write Error");
    }
}
