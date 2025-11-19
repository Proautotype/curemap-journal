package com.custard.journal_service.app.usecases.transcription;

import com.custard.journal_service.app.commands.transcript.CreateTranscriptCommand;
import com.custard.journal_service.app.usecases.UseCase;
import com.custard.journal_service.domain.models.Transcript;
import reactor.core.publisher.Mono;

public interface CreateTranscriptUseCase extends UseCase<CreateTranscriptCommand, Mono<Transcript>> {
}
