package com.makura.runtime.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

/**
 * Metrics collection for translation operations
 */
@Component
public class TranslationMetrics {

    private final MeterRegistry meterRegistry;

    public TranslationMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordTranslationRequest(String routeId) {
        Counter.builder("makura.translation.requests.total")
            .description("Total number of translation requests")
            .tag("routeId", routeId)
            .register(meterRegistry)
            .increment();
    }

    public void recordTranslationSuccess(String routeId) {
        Counter.builder("makura.translation.success.total")
            .description("Total number of successful translations")
            .tag("routeId", routeId)
            .tag("status", "success")
            .register(meterRegistry)
            .increment();
    }

    public void recordTranslationError(String routeId, String errorType) {
        Counter.builder("makura.translation.errors.total")
            .description("Total number of failed translations")
            .tag("routeId", routeId)
            .tag("status", "error")
            .tag("errorType", errorType)
            .register(meterRegistry)
            .increment();
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void recordDuration(Timer.Sample sample, String routeId) {
        Timer timer = Timer.builder("makura.translation.duration")
            .description("Translation request duration")
            .tag("routeId", routeId)
            .register(meterRegistry);
        sample.stop(timer);
    }
}

