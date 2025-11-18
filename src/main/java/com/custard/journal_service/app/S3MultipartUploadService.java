package com.custard.journal_service.app;

import com.custard.journal_service.infrastructure.S3Config;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.model.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class S3MultipartUploadService {

    private final S3AsyncClient s3Client;
    private final S3Config s3Config;
    private final Logger logger = LoggerFactory.getLogger(S3MultipartUploadService.class);

    private Mono<String> start(String key, String contentType) {

        CreateMultipartUploadRequest req = CreateMultipartUploadRequest.builder()
                .bucket(s3Config.getBucketName())
                .key(key)
                .contentType(contentType)
                .build();

        // createMultipartUpload
        return Mono.fromFuture(() -> s3Client.createMultipartUpload(req))
                .map(obj -> {
                    return obj;
                })
                .map(CreateMultipartUploadResponse::uploadId);
    }

    private Mono<CompletedPart> uploadOnePart(
            String key, String uploadId, int partNumber, ByteBuffer bytes
    ) {
        UploadPartRequest uploadPartRequest = UploadPartRequest.builder()
                .bucket(s3Config.getBucketName())
                .key(key)
                .uploadId(uploadId)
                .partNumber(partNumber)
                .contentLength((long) bytes.remaining())
                .build();

        return Mono.fromFuture(() -> s3Client.uploadPart(uploadPartRequest, AsyncRequestBody.fromByteBuffer(bytes)))
                .map(resp -> CompletedPart
                        .builder()
                        .partNumber(partNumber)
                        .eTag(resp.eTag())
                        .build());
    }

    // this method cumulates all parts of file and push
    private Mono<CompleteMultipartUploadResponse> completeUpload(
            String key,
            String uploadId,
            List<CompletedPart> parts
    ) {
        CompletedMultipartUpload multipart = CompletedMultipartUpload.builder()
                .parts(parts)
                .build();


        CompleteMultipartUploadRequest req = CompleteMultipartUploadRequest.builder()
                .bucket(s3Config.getBucketName())
                .key(s3Config.getBucketName())
                .uploadId(uploadId)
                .multipartUpload(multipart)
                .build();

        return Mono.fromFuture(() -> s3Client.completeMultipartUpload(req));

    }

    private Flux<ByteBuffer> chunkedFlux(Flux<DataBuffer> source, int partSize) {
        return Flux.create(sink -> {
            ByteArrayOutputStream acc = new ByteArrayOutputStream();

            source.subscribe(new BaseSubscriber<DataBuffer>() {
                @Override
                protected void hookOnNext(DataBuffer dataBuffer) {
//                    logger.info("hook-next {} ", dataBuffer.readableByteCount());

                    try {
                        byte[] incoming = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(incoming);
                        acc.write(incoming);
                    } catch (RuntimeException | IOException e) {
                        throw new RuntimeException(e);
                    } finally {
                        DataBufferUtils.release(dataBuffer);
                    }

                    // If accumulator reached partSize
                    while (acc.size() >= partSize) {
                        byte[] full = acc.toByteArray();

                        // Emit exactly partSize
                        byte[] emit = Arrays.copyOfRange(full, 0, partSize);
                        sink.next(ByteBuffer.wrap(emit));

                        // Reset accumulator with leftover
                        byte[] leftOver = Arrays.copyOfRange(full, partSize, full.length);
                        acc.reset();
                        try {
                            acc.write(leftOver);
                        } catch (IOException e) {
                            sink.error(e);
                        }
                    }
                }

                @Override
                protected void hookOnComplete() {
                    // Emit leftover (if any)
                    if (acc.size() > 0) {
                        sink.next(ByteBuffer.wrap(acc.toByteArray()));
                    }
                    sink.complete();
                }

                @Override
                protected void hookOnError(Throwable throwable) {
                    sink.error(throwable);
                }
            });
        });
    }

    private Flux<CompletedPart> uploadPartsSequentially(String key, String uploadId, Flux<ByteBuffer> chunks) {
        AtomicInteger counter = new AtomicInteger(1);
        logger.info("doing upload parts sequentially ");
        return chunks.concatMap(chunk -> {
            int partNumber = counter.getAndIncrement();

            logger.info("Uploading part {} with chunk {}  ", partNumber, chunk);

            return uploadOnePart(key, uploadId, partNumber, chunk);
        });
    }

    public Mono<String> uploadLargeFile(FilePart filePart) {
        String key = "upload/" + filePart.filename();
        String contentType = Optional.ofNullable(filePart.headers().getContentType())
                .map(MediaType::toString)
                .orElse("application/octet-stream");
        int PART_SIZE = 2 * 1024 * 1024; // 5MB

        // Create multipart upload
        return start(key, contentType)
                .flatMap(uploadId -> {
                    logger.info("new upload id {} ", uploadId);
                    Flux<ByteBuffer> chunks = chunkedFlux(filePart.content(), PART_SIZE);
                    Flux<CompletedPart> uploadedParts = uploadPartsSequentially(key, uploadId, chunks);
                    return uploadedParts.collectList()
                            .flatMap(parts -> completeUpload(key, uploadId, parts))
                            .map(resp -> "Upload complete for key: " + key);
                });
    }

}
