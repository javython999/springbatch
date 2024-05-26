package com.study.springbatch.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FlatFileItem {
    private long id;
    private String name;
    private int age;
}
