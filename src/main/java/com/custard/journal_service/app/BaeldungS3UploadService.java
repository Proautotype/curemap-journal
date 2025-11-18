package com.custard.journal_service.app;

import com.custard.journal_service.adapter.UploadResource;
import com.custard.journal_service.domain.UploadState;
import com.custard.journal_service.infrastructure.S3Config;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BaeldungS3UploadService {

    private final S3Config config;
    private final S3AsyncClient s3client;
    private final Logger logger = LoggerFactory.getLogger(UploadResource.class);

    public Mono<String> saveFile(HttpHeaders headers, FilePart part) {
        String fileKey = UUID.randomUUID().toString();
        Map<String, String> metadata = new HashMap<>();
        String fileName = part.filename();
        if (fileName == null) {
            fileName = fileKey;
        }
        metadata.put("fileName", fileName);


        UploadState uploadState = new UploadState(config.getBucketName(), fileKey);

        // 1. First create the multipart upload
        return Mono.fromFuture(() -> {
                    MediaType mediaType = part.headers().getContentType();
                    if (mediaType == null) {
                        mediaType = MediaType.APPLICATION_OCTET_STREAM;
                    }
                    return s3client.createMultipartUpload(CreateMultipartUploadRequest.builder()
                            .key(fileKey)
                            .bucket(config.getBucketName())
                            .metadata(metadata)
                            .contentType(mediaType.toString())
                            .build());
                }
        ).flatMap(response -> {
            uploadState.setUploadId(response.uploadId());
            logger.info("Created multipart upload with ID: {}", response.uploadId());

            // 2. Process the file content in chunks
            return part.content()
                    .bufferUntil(buffer -> {
                        uploadState.setBuffered(uploadState.getBuffered() + buffer.readableByteCount());
                        boolean endOfBuffer = uploadState.getBuffered() >= config.getMultipartMinPartSize();
                        if (endOfBuffer) {
                            uploadState.setBuffered(0);
                        }
                        return endOfBuffer;
                    })
                    .flatMap(buffers -> {
                        // 3. Upload each part
                        ByteBuffer buffer = concatBuffers(buffers);
                        int partNumber = uploadState.getPartCounter() + 1;
                        uploadState.setPartCounter(partNumber);

                        return uploadPart(uploadState, buffer, partNumber)
                                .doOnNext(completedPart -> {
                                    uploadState.getCompletedParts().put(partNumber, completedPart);
                                    logger.info("Uploaded part {} with ETag: {}", partNumber, completedPart.eTag());
                                });
                    }, 5) // Limit concurrency to 5 uploads at a time
                    .then(Mono.just(uploadState));
        }).flatMap(state -> {
            // 4. Complete the upload
            if (state.getCompletedParts().isEmpty()) {
                return Mono.error(new IllegalStateException("No parts were uploaded"));
            }
            logger.info("Completing upload with {} parts", state.getCompletedParts().size());
            return completeUpload(state);
        }).map(response -> {
            logger.info("Upload completed: {}", response.location());
            return fileKey;
        }).onErrorResume(e -> {
            logger.error("Error during file upload", e);
            // Optionally abort the upload if it was started
            if (uploadState.getUploadId() != null) {
                return abortUpload(uploadState)
                        .then(Mono.error(new RuntimeException("Upload failed, aborted multipart upload", e)));
            }
            return Mono.error(new RuntimeException("Upload failed", e));
        });
    }

    private Mono<CompletedPart> uploadPart(UploadState state, ByteBuffer buffer, int partNumber) {
        UploadPartRequest uploadRequest = UploadPartRequest.builder()
                .bucket(state.getBucket())
                .key(state.getFileKey())
                .uploadId(state.getUploadId())
                .partNumber(partNumber)
                .contentLength((long) buffer.remaining())
                .build();

        return Mono.fromFuture(() ->
                s3client.uploadPart(uploadRequest, AsyncRequestBody.fromByteBuffer(buffer))
                        .thenApply(uploadPartResponse ->
                                CompletedPart.builder()
                                        .eTag(uploadPartResponse.eTag())
                                        .partNumber(partNumber)
                                        .build()
                        )
        );
    }

    private Mono<CompleteMultipartUploadResponse> completeUpload(UploadState state) {
        CompletedMultipartUpload multipartUpload = CompletedMultipartUpload.builder()
                .parts(state.getCompletedParts().values())
                .build();

        return Mono.fromFuture(() ->
                s3client.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
                        .bucket(state.getBucket())
                        .key(state.getFileKey())
                        .uploadId(state.getUploadId())
                        .multipartUpload(multipartUpload)
                        .build())
        );
    }

    private Mono<AbortMultipartUploadResponse> abortUpload(UploadState state) {
        return Mono.fromFuture(() ->
                s3client.abortMultipartUpload(AbortMultipartUploadRequest.builder()
                        .bucket(state.getBucket())
                        .key(state.getFileKey())
                        .uploadId(state.getUploadId())
                        .build())
        ).doOnSuccess(r -> logger.info("Aborted upload {}", state.getUploadId()));
    }

    private ByteBuffer concatBuffers(List<DataBuffer> buffers) {
        // Calculate total size needed
        int totalSize = buffers.stream()
                .mapToInt(DataBuffer::readableByteCount)
                .sum();

        // Create a new ByteBuffer with the total size
        ByteBuffer combined = ByteBuffer.allocate(totalSize);

        // Copy each buffer's content
        for (DataBuffer buffer : buffers) {
            try {
                byte[] bytes = new byte[buffer.readableByteCount()];
                buffer.read(bytes);
                combined.put(bytes);
            } finally {
                // Always release the buffer to avoid memory leaks
                DataBufferUtils.release(buffer);
            }
        }

        // Prepare the buffer for reading
        combined.flip();
        return combined;
    }
}
