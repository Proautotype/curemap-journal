package com.custard.journal_service.app.exceptions;

import com.custard.journal_service.adapter.dto.FailureApiResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.reactive.result.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class ExceptionManager extends ResponseEntityExceptionHandler {

    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        Map<String, String> validationErrors = new HashMap<>();

        List<ObjectError> validationErrorList = ex.getBindingResult().getAllErrors();
        FailureApiResponse<List<ObjectError>> failureApiResponse = new FailureApiResponse<>();
        validationErrorList.forEach((error -> {
            String fieldName = ((FieldError) error).getField();
            String validationMsg = error.getDefaultMessage();
            validationErrors.put(fieldName, validationMsg);
        }));
        failureApiResponse.setData(validationErrorList);
        failureApiResponse.setMessage("Validation exception");
        return new ResponseEntity<>(failureApiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<FailureApiResponse<String>> handleGlobalException(
            Exception exception, WebRequest webRequest
    ) {
        FailureApiResponse<String> failureApiResponse = new FailureApiResponse<>();
        failureApiResponse.setCode("01");
        failureApiResponse.setMessage(exception.getMessage());
        failureApiResponse.setData(String.format("An error occurred : %s ", webRequest.getDescription(false)));
        return new ResponseEntity<>(
                failureApiResponse, HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<FailureApiResponse<String>> handleEntityNotFoundException(
            EntityNotFoundException exception,
            WebRequest request
    ) {
        FailureApiResponse<String> failureApiResponse = new FailureApiResponse<>();
        failureApiResponse.setCode("01");
        failureApiResponse.setMessage(exception.getMessage());
        failureApiResponse.setData(String.format("A not found exception occurred : %s", request.getDescription(false)));
        return new ResponseEntity<>(
                failureApiResponse,
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(EntityAlreadyExistException.class)
    public ResponseEntity<FailureApiResponse<String>> handleEntityAlreadyExistException(
            EntityAlreadyExistException exception,
            WebRequest request
    ) {
        FailureApiResponse<String> failureApiResponse = new FailureApiResponse<>();
        failureApiResponse.setCode("01");
        failureApiResponse.setMessage(exception.getMessage());
        failureApiResponse.setData(String.format("A conflict exception occurred : %s", request.getDescription(false)));
        return new ResponseEntity<>(
                failureApiResponse,
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(InaccurateInputException.class)
    public ResponseEntity<FailureApiResponse<String>> handleInaccurateInputException(
            InaccurateInputException exception,
            WebRequest request
    ){
        FailureApiResponse<String> failureApiResponse = new FailureApiResponse<>();
        failureApiResponse.setCode("01");
        failureApiResponse.setMessage(exception.getMessage());
        return new ResponseEntity<>(
                failureApiResponse,
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<FailureApiResponse<String>> handleAuthenticationException(
            AuthenticationException authenticationException,
            WebRequest request
    ){
        FailureApiResponse<String> failureApiResponse = new FailureApiResponse<>();
        failureApiResponse.setCode("01");
        failureApiResponse.setMessage(authenticationException.getMessage());
        return new ResponseEntity<>(
                failureApiResponse,
                HttpStatus.UNAUTHORIZED
        );
    }

}
