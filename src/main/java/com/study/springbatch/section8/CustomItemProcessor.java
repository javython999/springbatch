package com.study.springbatch.section8;

import com.study.springbatch.entity.Customer;
import org.springframework.batch.item.ItemProcessor;

public class CustomItemProcessor implements ItemProcessor<Customer, Customer> {


    @Override
    public Customer process(Customer customer) throws Exception {
        System.out.println("CustomItemProcessor process called");
        System.out.println("set UpperCase");
        //customer.setName(customer.getName().toUpperCase());
        return customer;
    }
}
