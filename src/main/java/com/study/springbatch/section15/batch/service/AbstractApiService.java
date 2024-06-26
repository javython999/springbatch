package com.study.springbatch.section15.batch.service;

import com.study.springbatch.section15.batch.domain.ApiInfo;
import com.study.springbatch.section15.batch.domain.ApiRequestVO;
import com.study.springbatch.section15.batch.domain.ApiResponseVO;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;

public abstract class AbstractApiService {

    public ApiResponseVO service(List<? extends ApiRequestVO> apiRequests) {

        RestTemplate restTemplate = new RestTemplateBuilder().errorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }

            @Override
            public void handleError(ClientHttpResponse response) throws IOException {

            }
        }).build();

        restTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        ApiInfo apiInfo = ApiInfo.builder().apiRequestList(apiRequests).build();

        return doApiService(restTemplate, apiInfo);
    }

    protected abstract ApiResponseVO doApiService(RestTemplate restTemplate, ApiInfo apiInfo);
}
