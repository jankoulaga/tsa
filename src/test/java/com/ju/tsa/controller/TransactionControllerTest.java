package com.ju.tsa.controller;

import com.ju.tsa.service.StatsService;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

public class TransactionControllerTest {
    private StatsService stats = new StatsService();

    @Test
    public void registerAValidTrx() throws Exception {
        MockMvc controllerMock = standaloneSetup(new TransactionsController(stats)).build();


        controllerMock.perform(post("/transactions")
                .content("{\"amount\" : 99.32, \"timestamp\": 99999999}")
                .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andExpect(status().isCreated());
    }

    @Test
    public void returnsAnErrorOnFaultyData() throws Exception {
        MockMvc controllerMock = standaloneSetup(new TransactionsController(stats)).build();
        controllerMock.perform(post("/transactions")
                .content("")
                .contentType(MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andExpect(status().isBadRequest());
    }

}
