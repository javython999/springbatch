package com.study.springbatch.section9.db.jpaItemWriter;

import com.study.springbatch.entity.Person;
import org.modelmapper.ModelMapper;
import org.springframework.batch.item.ItemProcessor;

public class PersonItemProcessor implements ItemProcessor<Person, PersonCopy> {

    ModelMapper modelMapper = new ModelMapper();

    @Override
    public PersonCopy process(Person item) throws Exception {
        return modelMapper.map(item, PersonCopy.class);
    }
}
