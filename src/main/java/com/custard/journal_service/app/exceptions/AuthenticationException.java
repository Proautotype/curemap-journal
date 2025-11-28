package com.custard.journal_service.app.exceptions;

public class AuthenticationException extends RuntimeException{

    public AuthenticationException(){
        super("Authentication failed");
    }
    public AuthenticationException(String message) {
        super(message);
    }
}
