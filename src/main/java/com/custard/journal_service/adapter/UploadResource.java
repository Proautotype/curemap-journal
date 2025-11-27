package com.custard.journal_service.adapter;

import com.custard.journal_service.app.BaeldungS3DownloadService;
import com.custard.journal_service.app.BaeldungS3UploadService;
import com.custard.journal_service.app.commands.journal.GetUserJournalsCommand;
import com.custard.journal_service.app.usecases.journal.GetUserJournalsUseCase;
import com.custard.journal_service.domain.models.Journal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

/**
 * REST controller for handling file uploads and downloads in the Journal Service.
 * Provides endpoints for uploading journal files, downloading files, and retrieving user journal records.
 */
@RestController
@RequestMapping("/api/v1/journal")
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Journal File Operations",
        description = "APIs for uploading, downloading and managing journal files")
public class UploadResource {

    private final BaeldungS3UploadService uploadService;
    private final BaeldungS3DownloadService downloadService;
    private final GetUserJournalsUseCase getUserJournalsUseCase;

    /**
     * Handles multipart file uploads for journal entries.
     *
     * @param headers HTTP headers from the request
     * @param parts The file part to be uploaded
     * @return A Mono containing the upload result as a String
     */
    @Operation(
            summary = "Upload a journal file",
            description = "Uploads a file to the journal service with the provided metadata in headers",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "File uploaded successfully",
                            content = @Content(schema = @Schema(implementation = String.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Invalid file or request format",
                            content = @Content(schema = @Schema(implementation = String.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "Internal server error during file upload"
                    )
            }
    )
    @RequestMapping(
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            method = {RequestMethod.POST, RequestMethod.PUT},
            path = "/upload"
    )
    public Mono<String> multipartUploadHandler(
            @RequestHeader HttpHeaders headers,
            @RequestPart("file") @Parameter(description = "The file to be uploaded") FilePart parts) {

        log.info("Starting file upload");
        return uploadService.saveFile(headers, parts)
                .doOnSuccess(result -> log.info("File uploaded successfully"))
                .doOnError(error -> log.error("Error during file upload", error));
    }

    /**
     * Retrieves all journal records for a specific user.
     *
     * @param userId The ID of the user whose journal records to retrieve
     * @return A Flux of Journal entries for the specified user
     */
    @Operation(
            summary = "Get user journal records",
            description = "Retrieves all journal entries for a specific user",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Successfully retrieved user journal records",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Journal.class)))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "User not found",
                            content = @Content(schema = @Schema(implementation = String.class))
                    )
            }
    )
    @GetMapping("/get-user-records/{userId}")
    public Flux<Journal> getUserRecords(
            @PathVariable("userId")
            @Parameter(description = "ID of the user to retrieve journal records for")
            String userId) {

        log.info("Retrieving journal records for user: {}", userId);
        return getUserJournalsUseCase.execute(new GetUserJournalsCommand(userId))
                .doOnComplete(() -> log.info("Successfully retrieved records for user: {}", userId))
                .doOnError(error -> log.error("Error retrieving records for user: {}", userId, error));
    }

    /**
     * Downloads a file with the specified file key.
     *
     * @param fileKey The unique identifier of the file to download
     * @return A Mono containing the file data as a ResponseEntity with ByteBuffer
     */
    @Operation(
            summary = "Download a file",
            description = "Downloads a file from the journal service using its unique file key",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "File downloaded successfully",
                            content = @Content(mediaType = "application/octet-stream")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "File not found with the specified key",
                            content = @Content(schema = @Schema(implementation = String.class))
                    )
            }
    )
    @GetMapping("/{fileKey}")
    public Mono<ResponseEntity<Flux<ByteBuffer>>> downloadFile(
            @PathVariable("fileKey")
            @Parameter(description = "Unique identifier of the file to download")
            String fileKey) {

        log.info("Downloading file with key: {}", fileKey);
        return downloadService.downloadFile(fileKey)
                .doOnSuccess(response -> log.info("File download initiated for key: {}", fileKey))
                .doOnError(error -> log.error("Error downloading file with key: {}", fileKey, error));
    }
}