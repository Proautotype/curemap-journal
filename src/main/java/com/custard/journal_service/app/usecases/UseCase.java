package com.custard.journal_service.app.usecases;

public interface UseCase <T,R>{
    R execute(T command);
}
