package com.custard.journal_service.app.usecases.transcription.impl;

import com.custard.journal_service.app.commands.transcript.CreateTranscriptCommand;
import com.custard.journal_service.app.mappers.TranscriptMapper;
import com.custard.journal_service.app.usecases.transcription.CreateTranscriptUseCase;
import com.custard.journal_service.domain.models.Transcript;
import com.custard.journal_service.domain.repository.TranscriptRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CreateTranscriptUseCaseImpl implements CreateTranscriptUseCase {

    private final Logger logger = LoggerFactory.getLogger(CreateTranscriptUseCaseImpl.class);
    private final TranscriptMapper transcriptMapper;
    private final TranscriptRepository transcriptRepository;

    @Override
    public Mono<Transcript> execute(CreateTranscriptCommand command) {
        logger.info("creating transcription ");
        Transcript transcript = transcriptMapper.toModel(command);

        return  transcriptRepository.save(transcript);
    }

}
