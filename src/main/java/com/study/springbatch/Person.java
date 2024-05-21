package com.study.springbatch;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@ToString
public class Person {

    @Id
    @GeneratedValue
    private long id;
    private String username;
    private int age;

    @OneToOne(mappedBy = "person")
    private Address address;


}
