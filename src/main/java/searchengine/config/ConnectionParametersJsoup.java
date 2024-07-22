package searchengine.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "connection-parameters")
public class ConnectionParametersJsoup {
    private String userAgent;
    private String referrer;
}
