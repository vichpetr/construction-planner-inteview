package eu.petrvich.construction.planner.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Thread-safe configuration properties for the Construction Planner application.
 * All fields are effectively immutable after Spring initialization, providing strong thread-safety guarantees.
 */
@Configuration
@ConfigurationProperties(prefix = "app")
@Data
public class AppProperties {

    private final Cache cache = new Cache();
    private final Logging logging = new Logging();

    @Data
    public static class Cache {
        /**
         * Enable/disable caching for project statistics.
         * Default: true
         */
        private boolean enabled = true;
    }

    @Data
    public static class Logging {
        private final RequestResponse requestResponse = new RequestResponse();

        @Data
        public static class RequestResponse {
            /**
             * Enable/disable request/response body logging.
             * Default: false
             */
            private boolean enabled = false;
        }
    }
}
