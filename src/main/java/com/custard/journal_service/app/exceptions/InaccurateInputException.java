package com.custard.journal_service.app.exceptions;

public class InaccurateInputException extends RuntimeException {

    public InaccurateInputException(){
        super("Invalid input check your inputs");
    }
    public InaccurateInputException(String message) {
        super(message);
    }
}
