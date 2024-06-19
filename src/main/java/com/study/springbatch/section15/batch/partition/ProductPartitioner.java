package com.study.springbatch.section15.batch.partition;

import com.study.springbatch.section15.batch.domain.ProductVO;
import com.study.springbatch.section15.batch.job.api.QueryGenerator;
import lombok.Setter;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ExecutionContext;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Setter
public class ProductPartitioner implements Partitioner {

    private DataSource dataSource;

    @Override
    public Map<String, ExecutionContext> partition(int gridSize) {

        ProductVO[] productList = QueryGenerator.getProductList(dataSource);

        Map<String, ExecutionContext> result = new HashMap<>();

        int number = 0;

        for (int i = 0; i < productList.length; i++) {
            ExecutionContext value = new ExecutionContext();

            result.put("partition" + number, value);
            value.put("product", productList[i]);

            number++;
        }

        return Map.of();
    }
}
