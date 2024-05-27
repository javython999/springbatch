package com.study.springbatch.section10.compositeItemprocessor;

import org.springframework.batch.item.ItemProcessor;

public class CustomItemProcessor2 implements ItemProcessor<String, String> {

    int index = 0;

    @Override
    public String process(String item) throws Exception {
        index++;
        return item + "-" + index;
    }
}
