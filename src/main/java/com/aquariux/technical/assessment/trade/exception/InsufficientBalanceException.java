package com.aquariux.technical.assessment.trade.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientBalanceException extends RuntimeException {

    private final String symbol;

    public InsufficientBalanceException(String symbol) {
        super ("Not enough balance for " + symbol);
        this.symbol = symbol;
    }

}
