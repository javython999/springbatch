package com.study.springbatch.section9.itemWriterAdapter;

public class CustomService<T> {

    public void customWrite(T item) {
        System.out.println(item);
    }
}
