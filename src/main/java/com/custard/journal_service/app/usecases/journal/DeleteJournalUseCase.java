package com.custard.journal_service.app.usecases.journal;

import com.custard.journal_service.app.commands.journal.DeleteJournalCommand;
import com.custard.journal_service.app.usecases.UseCase;
import reactor.core.publisher.Mono;

public interface DeleteJournalUseCase extends UseCase<DeleteJournalCommand, Mono<Void>> {
}
