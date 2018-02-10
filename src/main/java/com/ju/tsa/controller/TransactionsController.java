package com.ju.tsa.controller;

import com.ju.tsa.model.Transaction;
import com.ju.tsa.service.StatsService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Receives the Transction objects as JSON on the `transactions` path
 */
@RestController
public class TransactionsController {
    private final StatsService statsService;

    public TransactionsController(StatsService statsService) {
        this.statsService = statsService;
    }


    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(path = "/transactions")
    public void postTransaction(@RequestBody Transaction transaction) {
        statsService.add(transaction);
    }
}
