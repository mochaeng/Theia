package com.mochaeng.theia_api.shared.infrastructure.helpers;

import java.time.Duration;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;

public class SharedConfigHelpers {

    public static RetryTemplate getRetryTemplate(
        RetryTemplate retryTemplate,
        RetryPolicy retryPolicy,
        Duration retryDelay,
        float retryMultiplier
    ) {
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(retryDelay.toMillis());
        backOffPolicy.setMultiplier(retryMultiplier);
        backOffPolicy.setMaxInterval(30_000);

        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        return retryTemplate;
    }
}
