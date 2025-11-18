package com.custard.journal_service.infrastructure;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class S3AsyncClientConfiguration {
    private final S3Config s3Config;

    @Bean
    public S3AsyncClient asyncClient() {

        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                s3Config.getAccessKeyId(),
                s3Config.getSecretAccessKey()
        );

        S3Configuration serviceConfiguration = S3Configuration.builder()
                .checksumValidationEnabled(false)
                .chunkedEncodingEnabled(true)
                .pathStyleAccessEnabled(true) // ← REQUIRED FOR LOCALSTACK
                .build();

        SdkAsyncHttpClient httpClient = NettyNioAsyncHttpClient.builder()
                .writeTimeout(Duration.ZERO)
                .maxConcurrency(64)
                .build();

        S3AsyncClientBuilder b = S3AsyncClient.builder().httpClient(httpClient)
                .region(s3Config.getRegion())
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .serviceConfiguration(serviceConfiguration);

        if (s3Config.getS3().getEndpoint() != null) {

            System.out.println("endpoint -> " + s3Config.getEndpointAsUri().toString());

            b = b.endpointOverride(s3Config.getEndpointAsUri());
        }

        return b.build();
    }

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        if (StringUtils.isBlank(s3Config.getAccessKeyId())) {
            return DefaultCredentialsProvider.create();
        } else {
            return () -> AwsBasicCredentials.create(
                    s3Config.getAccessKeyId(),
                    s3Config.getSecretAccessKey());
        }
    }
}

