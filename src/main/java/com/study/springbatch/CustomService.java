package com.study.springbatch;

public class CustomService<T> {

    private int cnt = 0;

    public T customRead() {

        if (cnt == 30) return null;

        return (T) ("item" + cnt++);
    }
}
