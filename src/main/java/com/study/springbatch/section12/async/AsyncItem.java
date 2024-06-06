package com.study.springbatch.section12.async;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class AsyncItem {

    private long id;
    private String firstName;
    private String lastName;
    private String birthdate;
}
