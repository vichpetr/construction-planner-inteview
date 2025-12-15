package eu.petrvich.construction.planner.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread-safe filter for logging HTTP requests and responses.
 * Can be enabled/disabled at runtime via configuration.
 */
@Slf4j
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private final AtomicBoolean enabled;

    public RequestResponseLoggingFilter(boolean initialEnabled) {
        this.enabled = new AtomicBoolean(initialEnabled);
    }

    /**
     * Thread-safe method to enable/disable logging at runtime.
     */
    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    /**
     * Thread-safe check if logging is enabled.
     */
    public boolean isEnabled() {
        return this.enabled.get();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!isEnabled() || isActuatorEndpoint(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Wrap request and response to cache their content (max 10KB)
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request, 10240);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            logRequestResponse(requestWrapper, responseWrapper, duration);
            // Important: copy the cached response body back to the actual response
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequestResponse(ContentCachingRequestWrapper request,
                                    ContentCachingResponseWrapper response,
                                    long duration) {
        try {
            String requestBody = getContentAsString(request.getContentAsByteArray(), request.getCharacterEncoding());
            String responseBody = getContentAsString(response.getContentAsByteArray(), response.getCharacterEncoding());

            log.info("HTTP Request/Response Log:");
            log.info("  Method: {} {}", request.getMethod(), request.getRequestURI());
            log.info("  Query String: {}", request.getQueryString());
            log.info("  Status: {}", response.getStatus());
            log.info("  Duration: {} ms", duration);

            if (!requestBody.isEmpty()) {
                log.info("  Request Body: {}", requestBody);
            }

            if (!responseBody.isEmpty() && response.getStatus() >= 400) {
                log.info("  Response Body: {}", responseBody);
            }
        } catch (Exception e) {
            log.warn("Failed to log request/response", e);
        }
    }

    private String getContentAsString(byte[] content, String encoding) {
        if (content == null || content.length == 0) {
            return "";
        }
        try {
            return new String(content, encoding != null ? encoding : StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            log.warn("Failed to parse content", e);
            return "[Unable to parse content]";
        }
    }

    private boolean isActuatorEndpoint(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri != null && uri.startsWith("/actuator");
    }
}
