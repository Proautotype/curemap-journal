package com.custard.journal_service.adapter;

import com.custard.journal_service.app.BaeldungS3UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/inbox")
@Slf4j
@RequiredArgsConstructor
public class UploadResource {

    private final BaeldungS3UploadService uploadService;
    private final Logger logger = LoggerFactory.getLogger(UploadResource.class);

    @RequestMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            method = {RequestMethod.POST, RequestMethod.PUT})
    public Mono<String> multipartUploadHandler(
            @RequestHeader HttpHeaders headers,
            @RequestPart("file") FilePart parts) {


        Mono<String> data = uploadService.saveFile(headers, parts);
        logger.info("Uploading completed ");
        return data;
    }


}
