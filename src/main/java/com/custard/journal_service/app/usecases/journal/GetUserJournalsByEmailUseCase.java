package com.custard.journal_service.app.usecases.journal;

import com.custard.journal_service.app.commands.journal.GetUserJournalsCommand;
import com.custard.journal_service.app.usecases.UseCase;
import com.custard.journal_service.domain.models.Journal;
import reactor.core.publisher.Flux;

public interface GetUserJournalsByEmailUseCase extends
        UseCase<GetUserJournalsCommand, Flux<Journal>> {
}
