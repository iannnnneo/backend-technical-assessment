package com.aquariux.technical.assessment.trade.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomExceptionHandler {
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<String> handleInsufficientBalanceException(InsufficientBalanceException e) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
}
