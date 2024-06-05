package com.study.springbatch.section12.async;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class AsyncItemDto {
    private long id;
    private String firstName;
    private String lastName;
    private String birthDate;
}
