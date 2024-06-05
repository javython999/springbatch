package com.study.springbatch.section12.async;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AsyncItemRowMapper implements RowMapper<AsyncItemDto> {

    @Override
    public AsyncItemDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new AsyncItemDto(
                rs.getLong("id"),
                rs.getString("firstName"),
                rs.getString("lastName"),
                rs.getString("birthdate")
        );
    }
}
