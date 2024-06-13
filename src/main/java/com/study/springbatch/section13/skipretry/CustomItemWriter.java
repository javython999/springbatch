package com.study.springbatch.section13.skipretry;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

public class CustomItemWriter implements ItemWriter<String> {

    int count = 0;

    @Override
    public void write(Chunk<? extends String> chunk) throws Exception {
        for (String item : chunk.getItems()) {
            if (count < 2) {
                if (count % 2 == 0) {
                    count++;
                }
                else if (count % 2 == 1) {
                    count++;
                    throw new CustomRetryException("failed");
                }
            }

            System.out.println("write : " + item);
        }
    }
}
