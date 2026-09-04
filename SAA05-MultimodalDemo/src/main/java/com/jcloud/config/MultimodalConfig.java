package com.jcloud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * @author chujunjie
 * @date create in 16:21 2026/9/2
 */
@Configuration
public class MultimodalConfig {

    /**
     * 设置模型访问重试策略，尤其在图片视频生成时
     *
     * @return
     */
    @Bean
    public RetryTemplate imageRetryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        // 1. 设置重试策略：最多重试 30 次（可根据需求调整）
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(30); // 最大重试次数
        retryTemplate.setRetryPolicy(retryPolicy);

        // 2. 设置退避策略：每次重试间隔 2 秒（2000 毫秒）
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(2000L); // 每次重试间隔2秒
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }
}
