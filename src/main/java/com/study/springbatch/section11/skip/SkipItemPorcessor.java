package com.study.springbatch.section11.skip;

import org.springframework.batch.item.ItemProcessor;

public class SkipItemPorcessor implements ItemProcessor<String, String> {

    private int count = 0;

    @Override
    public String process(String item) throws Exception {

        if (item.equals("6") || item.equals("7")) {
            throw new SkippableException("Process failed count = " + count);
        } else {
            System.out.println("ItemProcessor = " + item);
            return String.valueOf(Integer.parseInt(item) * -1);
        }
    }
}
