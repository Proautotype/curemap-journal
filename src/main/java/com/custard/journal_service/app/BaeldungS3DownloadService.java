package com.custard.journal_service.app;

import com.custard.journal_service.infrastructure.S3Config;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BaeldungS3DownloadService {

    private final S3Config s3config;
    private final S3AsyncClient s3client;

    private final Logger log = LoggerFactory.getLogger(BaeldungS3DownloadService.class);

    public Mono<ResponseEntity<Flux<ByteBuffer>>> downloadFile(String fileKey) {
        return Mono.fromCallable(() ->
                        GetObjectRequest.builder()
                                .bucket(s3config.getS3().getBucketName())
                                .key(fileKey)
                                .build()
                )
                .flatMap(request ->
                        Mono.fromFuture(s3client.getObject(request, AsyncResponseTransformer.toPublisher()))
                                .onErrorResume(S3Exception.class, e ->
                                        Mono.error(new ResponseStatusException(
                                                e.statusCode() == 404 ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR,
                                                "Failed to download file: " + e.getMessage(), e)
                                        )
                                )
                                .map(response -> {
                                    checkResult(response.response());
                                    String filename = getMetadataItem(response.response(), "filename", fileKey);
                                    String contentType = Optional.ofNullable(response.response().contentType())
                                            .orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);

                                    return ResponseEntity.ok()
                                            .header(HttpHeaders.CONTENT_TYPE, contentType)
                                            .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(response.response().contentLength()))
                                            .header(HttpHeaders.CONTENT_DISPOSITION,
                                                    "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8) + "\"")
                                            .cacheControl(CacheControl.maxAge(Duration.ofDays(1)))
                                            .eTag(response.response().eTag())
                                            .lastModified(response.response().lastModified())
                                            .body(Flux.from(response));
                                })
                );
    }

//    public Mono<ResponseEntity<Flux<ByteBuffer>>> downloadFile(String fileKey, ServerWebExchange exchange) {
//        return Mono.fromCallable(() ->
//                        GetObjectRequest.builder()
//                                .bucket(s3config.getS3().getBucketName())
//                                .key(fileKey)
//                                .range(exchange.getRequest().getHeaders().getFirst(HttpHeaders.RANGE))
//                                .build()
//                )
//                .flatMap(request ->
//                        Mono.fromFuture(s3client.getObject(request, AsyncResponseTransformer.toPublisher()))
//                                .onErrorResume(S3Exception.class, e ->
//                                        Mono.error(new ResponseStatusException(
//                                                e.statusCode() == 404 ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR,
//                                                "Failed to download file: " + e.getMessage(), e)
//                                        )
//                                )
//                                .flatMap(response -> {
//                                    checkResult(response.response());
//                                    String filename = getMetadataItem(response.response(), "filename", fileKey);
//                                    String contentType = Optional.ofNullable(response.response().contentType())
//                                            .orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);
//
//                                    long contentLength = response.response().contentLength();
//                                    String contentRange = response.response().contentRange();
//                                    boolean isPartial = contentRange != null;
//
//                                    // Log download attempt
//                                    log.debug("Downloading file: {} (Size: {}, Type: {})",
//                                            filename, formatFileSize(contentLength), contentType);
//
//                                    // Build response
//                                    ResponseEntity.BodyBuilder builder = ResponseEntity
//                                            .status(isPartial ? HttpStatus.PARTIAL_CONTENT : HttpStatus.OK)
//                                            .header(HttpHeaders.CONTENT_TYPE, contentType)
//                                            .header(HttpHeaders.ACCEPT_RANGES, "bytes")
//                                            .header(HttpHeaders.CONTENT_DISPOSITION,
//                                                    "attachment; filename=\"" + URLEncoder.encode(filename, StandardCharsets.UTF_8) + "\"")
//                                            .cacheControl(CacheControl.maxAge(Duration.ofDays(1)))
//                                            .eTag(response.response().eTag())
//                                            .lastModified(response.response().lastModified());
//
//                                    // Add content length if not a partial request
//                                    if (!isPartial) {
//                                        builder.contentLength(contentLength);
//                                    }
//
//                                    // Add content range for partial content
//                                    if (contentRange != null) {
//                                        builder.header(HttpHeaders.CONTENT_RANGE, contentRange);
//                                    }
//
//                                    // Add rate limiting headers
//                                    exchange.getResponse().getHeaders().add("X-RateLimit-Limit", "1000");
//                                    exchange.getResponse().getHeaders().add("X-RateLimit-Remaining", "999");
//                                    exchange.getResponse().getHeaders().add("X-RateLimit-Reset",
//                                            String.valueOf(System.currentTimeMillis() / 1000 + 3600));
//
//                                    return Mono.just(builder.body(Flux.from(response)));
//                                })
//                )
//                .doOnError(e -> log.error("Download failed for file: {}", fileKey, e))
//                .metrics()
//                .name("s3.download.requests")
//                .tag("file_key", fileKey)
//                .description("S3 file download metrics")
//                .register(registry);
//    }

    // Helper method to format file size
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "i";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }


    private String getMetadataItem(GetObjectResponse response, String metadataKey, String defaultValue) {
        if (response == null || response.metadata() == null) {
            return defaultValue;
        }
        return response.metadata().getOrDefault(metadataKey, defaultValue);
    }

    private void checkResult(software.amazon.awssdk.services.s3.model.GetObjectResponse response) {
        if (response == null) {
            throw new IllegalStateException("Empty response from S3");
        }
        if (response.sdkHttpResponse() != null && !response.sdkHttpResponse().isSuccessful()) {
            throw new IllegalStateException("Failed to download file from S3: " +
                    response.sdkHttpResponse().statusText().orElse("Unknown error"));
        }
    }

}
