package com.study.springbatch.section11.retry;


import org.springframework.batch.item.ItemProcessor;

public class RetryItemProcssor implements ItemProcessor<String, String> {

    private int count = 0;

    @Override
    public String process(String item) throws Exception {
        count++;
        System.out.println("count = " + count);
        throw new RetryableException("RetryableException");
    }
}
