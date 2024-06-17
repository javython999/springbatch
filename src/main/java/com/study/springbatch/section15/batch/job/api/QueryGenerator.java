package com.study.springbatch.section15.batch.job.api;

import com.study.springbatch.section15.batch.domain.ProductVO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryGenerator {

    public static ProductVO[] getProductList(DataSource dataSource) {
        JdbcTemplate jdbcTemplate  = new JdbcTemplate(dataSource);
        List<ProductVO> productList = jdbcTemplate.query("select type from product group by type", new RowMapper<ProductVO>() {

            @Override
            public ProductVO mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new ProductVO().builder().type(rs.getString("type")).build();
            }
        });
        return productList.toArray(new ProductVO[]{});
    }

    public static Map<String, Object> getParmaeterForQuery(String parameter, String value) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(parameter, value);
        return parameters;
    }
}
