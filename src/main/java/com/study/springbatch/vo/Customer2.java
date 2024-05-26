package com.study.springbatch.vo;


import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@Setter
@ToString
public class Customer2 {

    private Long id;
    private String firstName;
    private String lastName;
    private String birthdate;
}
