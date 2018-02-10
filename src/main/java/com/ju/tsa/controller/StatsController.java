package com.ju.tsa.controller;

import com.ju.tsa.model.TransactionAggregator;
import com.ju.tsa.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Serves the responses on the `statistics` endpoint
 */
@RestController
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }


    @GetMapping(path = "/statistics")
    public TransactionAggregator getStats() {
        return statsService.aggregate();
    }

}
