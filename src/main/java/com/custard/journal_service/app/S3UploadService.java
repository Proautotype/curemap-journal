package com.custard.journal_service.app;

import com.custard.journal_service.infrastructure.S3Config;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.ByteBuffer;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class S3UploadService {

    private final S3AsyncClient asyncClient;
    private final S3Config s3Config;
    private final DataBufferFactory bufferFactory = new DefaultDataBufferFactory();
    private final Logger logger = LoggerFactory.getLogger(S3UploadService.class);


    @PostConstruct
    void load() {
        logger.info("s3-config {} ", s3Config);
    }

    public Mono<String> upload(FilePart file) {
        String key = "uploads/" + System.currentTimeMillis() + "_" + file.filename();

        return file.content()
                .collectList()
                .flatMap(dataBuffers -> {
                    // Calculate total size
                    int size = dataBuffers.stream()
                            .mapToInt(DataBuffer::readableByteCount)
                            .sum();

                    // Combine all data buffers
                    ByteBuffer combined = ByteBuffer.allocate(size);
                    dataBuffers.forEach(buffer -> {
                        byte[] bytes = new byte[buffer.readableByteCount()];
                        buffer.read(bytes);
                        combined.put(bytes);
                        DataBufferUtils.release(buffer);
                    });
                    combined.flip();

                    // Create request body
                    AsyncRequestBody body = AsyncRequestBody.fromByteBuffer(combined);

                    // Build the request
                    PutObjectRequest req = PutObjectRequest.builder()
                            .bucket(s3Config.getS3().getBucketName())
                            .key(key)
                            .contentType(Optional.ofNullable(file.headers().getContentType())
                                    .map(MediaType::toString)
                                    .orElse("application/octet-stream"))
                            .contentLength((long) size)
                            .build();

                    // Execute the upload
                    return Mono.fromFuture(() ->
                            asyncClient.putObject(req, body)
                                    .thenApply(response -> "File uploaded successfully: " + key)
                    );
                });
    }

}
