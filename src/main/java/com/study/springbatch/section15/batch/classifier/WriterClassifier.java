package com.study.springbatch.section15.batch.classifier;

import com.study.springbatch.section15.batch.domain.ApiRequestVO;
import lombok.Setter;
import org.springframework.batch.item.ItemWriter;
import org.springframework.classify.Classifier;

import java.util.HashMap;
import java.util.Map;

@Setter
public class WriterClassifier<C, T> implements Classifier {

    private Map<String, ItemWriter<ApiRequestVO>> writerMap = new HashMap<>();

    @Override
    public Object classify(Object classifiable) {
        return writerMap.get(((ApiRequestVO) classifiable).getProductVO().getType());
    }
}
