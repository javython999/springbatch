package com.study.springbatch.section15.batch.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Product {

    @Id
    private Long id;
    private String name;
    private int price;
    private String type;
}
