package com.custard.journal_service.infrastructure;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;

import java.net.URI;

@Configuration
@ConfigurationProperties(prefix = "aws")
@Getter
@Setter
@ToString
public class S3Config {
    private String accessKeyId;
    private String secretAccessKey;
    private String region;
    private String bucketName;
    private String endpoint;
    private String multipartMinPartSize;

    public Region getRegion() {
        return Region.of(region);
    }

    public URI getEndpointAsUri() {
        return URI.create(endpoint);
    }

    public long getMultipartMinPartSize() {
        if (this.multipartMinPartSize != null) {
            return Long.parseLong(this.multipartMinPartSize);
        }
        return (5 * 1024 * 1024);
    }
}
