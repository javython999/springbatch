package com.study.springbatch.section11.retry;

import org.springframework.batch.item.ItemProcessor;
import org.springframework.classify.BinaryExceptionClassifier;
import org.springframework.classify.Classifier;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.DefaultRetryState;
import org.springframework.retry.support.RetryTemplate;

//@Component
public class RetryItemProcessor2 implements ItemProcessor<String, RetryCustomer> {

    //@Autowired
    private RetryTemplate retryTemplate;

    private int count = 0;

    @Override
    public RetryCustomer process(String item) throws Exception {

        Classifier<Throwable, Boolean> rollbackClassifier = new BinaryExceptionClassifier(true);


        return retryTemplate.execute(
                (RetryCallback<RetryCustomer, RuntimeException>) context -> {
                    if ("1".equals(item) || "2".equals(item)) {
                        count++;
                        System.out.println("retry Count = " + count);
                        throw new RetryableException("failed count = " + count);
                    }

                    return new RetryCustomer(item);
                }, (RecoveryCallback) context -> new RetryCustomer(item + ".Recover"), new DefaultRetryState(item, rollbackClassifier)
        );
    }
}
