package com.study.springbatch.section10.classifierCompositeItemprocessor;

import org.springframework.batch.item.ItemProcessor;

public class CustomItemProcessor1 implements ItemProcessor<ProcessorInfo, ProcessorInfo> {

    @Override
    public ProcessorInfo process(ProcessorInfo item) throws Exception {
        System.out.println("CustomItemProcessor1 process");
        return item;
    }
}
