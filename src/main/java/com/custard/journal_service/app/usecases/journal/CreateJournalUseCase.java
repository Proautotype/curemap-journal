package com.custard.journal_service.app.usecases.journal;

import com.custard.journal_service.app.commands.journal.CreateJournalCommand;
import com.custard.journal_service.app.usecases.UseCase;
import com.custard.journal_service.domain.models.Journal;
import reactor.core.publisher.Mono;

public interface CreateJournalUseCase  extends UseCase<CreateJournalCommand, Mono<Journal>> {
}
