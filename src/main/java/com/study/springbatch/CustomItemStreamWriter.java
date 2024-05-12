package com.study.springbatch;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamWriter;

public class CustomItemStreamWriter implements ItemStreamWriter<String> {

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        System.out.println("OPEN");
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        System.out.println("UPDATE");
    }

    @Override
    public void close() throws ItemStreamException {
        System.out.println("CLOSE");
    }


    @Override
    public void write(Chunk chunk) throws Exception {
        chunk.forEach(item -> System.out.println(item));
    }
}
