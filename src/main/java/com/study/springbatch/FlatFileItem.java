package com.study.springbatch;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FlatFileItem {
    private long id;
    private String name;
    private int age;
}
