package com.study.springbatch.section10.compositeItemprocessor;

import org.springframework.batch.item.ItemProcessor;

public class CustomItemProcessor1 implements ItemProcessor<String, String> {

    @Override
    public String process(String item) throws Exception {
        return item.toUpperCase();
    }
}
