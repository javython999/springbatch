package com.study.springbatch.section9.db.jpaItemWriter;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@ToString
public class PersonCopy {

    @Id
    private long id;
    private String username;
    private int age;

}
