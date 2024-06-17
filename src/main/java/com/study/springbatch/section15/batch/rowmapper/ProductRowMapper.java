package com.study.springbatch.section15.batch.rowmapper;

import com.study.springbatch.section15.batch.domain.ProductVO;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class ProductRowMapper implements RowMapper<ProductVO> {

    @Override
    public ProductVO mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new ProductVO().builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .price(rs.getInt("price"))
                .type(rs.getString("type"))
                .build();
    }
}
