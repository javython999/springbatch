package com.study.springbatch.section15.batch.classifier;

import com.study.springbatch.section15.batch.domain.ApiRequestVO;
import com.study.springbatch.section15.batch.domain.ProductVO;
import lombok.Setter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.classify.Classifier;

import java.util.HashMap;
import java.util.Map;

@Setter
public class ProcessorClassifier<C, T> implements Classifier {

    private Map<String, ItemProcessor<ProductVO, ApiRequestVO>> processorsMap = new HashMap<>();

    @Override
    public Object classify(Object classifiable) {
        return processorsMap.get(((ProductVO) classifiable).getType());
    }
}
