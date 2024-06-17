package com.study.springbatch.section15.batch.partition;

import lombok.Setter;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import javax.sql.DataSource;
import java.util.Map;

@Setter
public class ProductPartitioner implements Partitioner {

    private DataSource dataSource;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {
        return Map.of();
    }
}
