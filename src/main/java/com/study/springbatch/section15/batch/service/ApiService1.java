package com.study.springbatch.section15.batch.service;

import com.study.springbatch.section15.batch.domain.ApiInfo;
import com.study.springbatch.section15.batch.domain.ApiResponseVO;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ApiService1 extends AbstractApiService {

    @Override
    protected ApiResponseVO doApiService(RestTemplate restTemplate, ApiInfo apiInfo) {

        ResponseEntity<String> responseEntity = restTemplate.postForEntity("http://localhost:8081/api/product/1/", apiInfo, String.class);
        HttpStatusCode statusCode = responseEntity.getStatusCode();

        return  ApiResponseVO.builder()
                .status(statusCode.value())
                .message(responseEntity.getBody())
                .build();
    }
}
