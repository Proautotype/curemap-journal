package com.custard.journal_service.adapter;

import com.custard.journal_service.app.S3DownloadService;
import com.custard.journal_service.app.S3MultipartUploadService;
import com.custard.journal_service.app.S3UploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/media")
public class MediaController {
    private final S3UploadService uploadService;
    private final S3DownloadService s3DownloadService;
    private final S3MultipartUploadService s3MultipartUploadService;
    private final Logger logger = LoggerFactory.getLogger(MediaController.class);

    public MediaController(
            S3UploadService uploadService,
            S3DownloadService s3DownloadService,
            S3MultipartUploadService s3MultipartUploadService
    ) {
        this.uploadService = uploadService;
        this.s3DownloadService = s3DownloadService;
        this.s3MultipartUploadService = s3MultipartUploadService;
    }

    @PostMapping("/upload")
    public Mono<String> upload(@RequestPart("file") FilePart file) {
        logger.info("file received -> {} ", file.filename());
        return uploadService.upload(file);
    }

    @PostMapping("/upload2")
    public Mono<String> upload2(@RequestPart("file") FilePart file) {
        logger.info("file multipart received -> {} ", file.filename());
        return s3MultipartUploadService.uploadLargeFile(file);
    }

    @GetMapping("/{key}")
    public Mono<ResponseEntity<Flux<DataBuffer>>> stream(
            @PathVariable String key,
            @RequestHeader(value = "Range", required = false) String range
    ) {
        return s3DownloadService.stream(key, range);
    }
}
