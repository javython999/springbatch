package com.study.springbatch.section13.skipretry;

import org.springframework.aop.support.AopUtils;
import org.springframework.batch.item.ItemReader;

import java.util.LinkedList;
import java.util.List;

public class LinkedListItemReader2<T> implements ItemReader<T> {

    private List<T> list;

    public LinkedListItemReader2(List<T> list) {
        if (AopUtils.isAopProxy(list)) {
            this.list = list;
        } else {
            this.list = new LinkedList<>(list);
        }


    }


    @Override
    public T read() throws CustomRetryException {

        if (!list.isEmpty()) {
            T remove = (T) list.remove(0);
            if ((Integer) remove == 3) {
                throw new CustomRetryException("read skipped : " + remove);
            }
            System.out.println("read : " + remove);
            return remove;
        }

        return null;
    }
}
