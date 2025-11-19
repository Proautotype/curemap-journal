package com.custard.journal_service.app.usecases.journal;

import com.custard.journal_service.app.commands.journal.GetJournalByIdCommand;
import com.custard.journal_service.app.usecases.UseCase;
import com.custard.journal_service.domain.models.Journal;
import reactor.core.publisher.Mono;

public interface GetJournalByIdUseCase extends UseCase<GetJournalByIdCommand, Mono<Journal>> {
}
