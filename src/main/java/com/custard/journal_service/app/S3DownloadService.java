package com.custard.journal_service.app;

import com.custard.journal_service.infrastructure.S3Config;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncResponseTransformer;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class S3DownloadService {


    private final S3AsyncClient asyncClient;
    private final S3Config s3Config;
    private final DataBufferFactory bufferFactory = new DefaultDataBufferFactory();
    private final Logger logger = LoggerFactory.getLogger(S3DownloadService.class);
    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";


    public Mono<HeadObjectResponse> head(String bucket, String key) {
        return Mono.fromFuture(() ->
                asyncClient.headObject(
                        HeadObjectRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .build()
                )
        );
    }

    public Optional<long[]> validateRange(String header, long length) {
        if (header == null || !header.contains("bytes=")) return Optional.empty();

        try {
            String[] p = header.substring(6).split("-", 2);
            long start = p[0].isEmpty() ? 0 : Long.parseLong(p[0]);
            long end = p.length < 2 || p[1].isEmpty() ?
                    length - 1 :
                    Long.parseLong(p[1]);

            if (end < start) return Optional.empty();
            if (start < 0 || start >= length) return Optional.empty();
            if (end >= length) end = length - 1;

            return Optional.of(new long[]{start, end});

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public ResponseEntity<Flux<DataBuffer>> build416(long length) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Range", "bytes */" + length);
        return ResponseEntity.status(416).headers(headers).body(Flux.empty());
    }

    public Mono<ResponseEntity<Flux<DataBuffer>>> stream(String key, String rangeHeader) {
        return head(s3Config.getBucketName(), key)
                .flatMap(meta -> {
                    long total = meta.contentLength();

                    Optional<long[]> valid = validateRange(rangeHeader, total);

                    if (rangeHeader != null && valid.isEmpty()) {
                        return Mono.just(build416(total));
                    }
                    boolean isPartial = valid.isPresent();
                    String range = null;
                    if (isPartial) {
                        long start = valid.get()[0];
                        long end = valid.get()[1];
                        range = "bytes=" + start + "-" + end;
                    }

                    return fetchRange(key, range, total, isPartial);

                });
    }

    private Mono<ResponseEntity<Flux<DataBuffer>>> fetchRange(String key, String rangeHeader, long totalLength, boolean isPartial) {
        GetObjectRequest.Builder builder = GetObjectRequest.builder().bucket(s3Config.getBucketName()).key(key);

        if (rangeHeader != null) builder.range(rangeHeader);

        return Mono.fromFuture(() ->
                        asyncClient.getObject(builder.build(), AsyncResponseTransformer.toPublisher()))
                .flatMap(pub -> {
                    GetObjectResponse meta = pub.response();
                    Flux<DataBuffer> body = Flux.from(pub).map(bufferFactory::wrap);

                    HttpHeaders h = new HttpHeaders();
                    h.set("Accept-Ranges", "bytes");
                    h.setContentType(
                            MediaType.valueOf(
                                    meta.contentType() != null ? meta.contentType() : "application/octet-stream"
                            )
                    );

                    if (isPartial) {
                        long start = Long.parseLong(Objects.requireNonNull(rangeHeader).substring(6).split("-")[0]);
                        long end = Long.parseLong(rangeHeader.substring(6).split("-")[1]);
                        h.set("Content-Range",
                                "bytes " + start + "-" + end + "/" + totalLength);
                        h.setContentLength(end - start + 1);
                        return Mono.just(ResponseEntity.status(206).headers(h).body(body));
                    }
                    h.setContentLength(meta.contentLength());
                    return Mono.just(ResponseEntity.status(200).headers(h).body(body));
                });

    }

}
