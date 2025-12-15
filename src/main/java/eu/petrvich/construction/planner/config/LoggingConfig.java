package eu.petrvich.construction.planner.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * Configuration for request/response logging.
 * Can be enabled/disabled via configuration property: app.logging.request-response.enabled
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class LoggingConfig {

    private final AppProperties appProperties;

    @Bean
    @ConditionalOnProperty(name = "app.logging.request-response.enabled", havingValue = "true", matchIfMissing = false)
    public FilterRegistrationBean<RequestResponseLoggingFilter> requestResponseLoggingFilter() {
        boolean enabled = appProperties.getLogging().getRequestResponse().isEnabled();

        log.info("Configuring request/response logging filter (enabled: {})", enabled);

        RequestResponseLoggingFilter filter = new RequestResponseLoggingFilter(enabled);
        FilterRegistrationBean<RequestResponseLoggingFilter> registrationBean = new FilterRegistrationBean<>(filter);

        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        registrationBean.addUrlPatterns("/api/*");

        return registrationBean;
    }
}
