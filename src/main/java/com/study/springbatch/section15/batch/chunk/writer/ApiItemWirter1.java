package com.study.springbatch.section15.batch.chunk.writer;

import com.study.springbatch.section15.batch.domain.ApiRequestVO;
import com.study.springbatch.section15.batch.domain.ApiResponseVO;
import com.study.springbatch.section15.batch.service.AbstractApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;

import java.util.List;

@RequiredArgsConstructor
public class ApiItemWirter1 implements ItemWriter<ApiRequestVO> {

    private final AbstractApiService apiService;

    @Override
    public void write(Chunk<? extends ApiRequestVO> chunk) throws Exception {
        ApiResponseVO apiResponseVO = apiService.service((List<? extends ApiRequestVO>) chunk);
        System.out.println("apiResponseVO: " + apiResponseVO);
    }
}
