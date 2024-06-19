package com.study.springbatch.section15.batch.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponseVO {

    private int status;
    private String message;
}
