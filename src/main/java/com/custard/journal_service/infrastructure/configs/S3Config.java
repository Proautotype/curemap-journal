package com.custard.journal_service.infrastructure.configs;

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
    private S3 s3 = new S3();
    private String multipartMinPartSize;

    @Getter
    @Setter
    public static class S3 {
        private String bucketName;
        private String endpoint;
        private boolean pathStyleAccess;
    }

    public Region getRegion() {
        return Region.of(region);
    }

    public URI getEndpointAsUri() {
        if (s3 != null && s3.getEndpoint() != null) {
            return URI.create(s3.getEndpoint().startsWith("http") ? 
                s3.getEndpoint() : 
                "http://" + s3.getEndpoint());
        }
        return null;
    }

    public long getMultipartMinPartSize() {
        if (this.multipartMinPartSize != null) {
            return Long.parseLong(this.multipartMinPartSize);
        }
        return (5 * 1024 * 1024);
    }
}
