package com.study.springbatch;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@ToString
public class Address {

    @Id
    @GeneratedValue
    private long id;
    private String location;

    @OneToOne
    @JoinColumn(name = "person_id")
    private Person person;
}
