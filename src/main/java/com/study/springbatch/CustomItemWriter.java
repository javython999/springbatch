package com.study.springbatch;

import com.study.springbatch.entity.Customer;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

public class CustomItemWriter implements ItemWriter<Customer> {

    @Override
    public void write(Chunk<? extends Customer> chunk) throws Exception {
        System.out.println("CustomItemWriter written");
        chunk.forEach(item -> System.out.println(item));
    }
}
