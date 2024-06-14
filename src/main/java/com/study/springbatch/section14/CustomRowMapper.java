package com.study.springbatch.section14;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomRowMapper implements RowMapper<CustomerDTO> {

    @Override
    public CustomerDTO mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new CustomerDTO(
                rs.getLong("id"),
                rs.getString("firstName"),
                rs.getString("lastName"),
                rs.getString("birthdate")
        );
    }
}
