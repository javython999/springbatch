package com.study.springbatch.section11.skip;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

public class SkipItemWriter implements ItemWriter<String> {

    private int count = 0;

    @Override
    public void write(Chunk<? extends String> chunk) throws Exception {
        for (String item : chunk.getItems()) {
            if ("-12".equals(item)) {
                throw new SkippableException("Write failed count = " + count);
            } else {
                System.out.println("ItemWriter = " + item);
            }
        }
    }
}
