package com.ian.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ian.account.dto.*;
import com.ian.account.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static com.ian.account.type.TransactionResultType.S;
import static com.ian.account.type.TransactionType.USE;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(TransactionController.class)
@Import(TransactionControllerTest.MockConfig.class)
class TransactionControllerTest {
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void useBalanceSuccess() throws Exception {
        given(transactionService.useBalance(anyLong(), anyString(), anyLong()))
                .willReturn(TransactionDTO.builder()
                        .accountNumber("1000000000")
                        .amount(12345L)
                        .transactionId("transactionId")
                        .transactionResultType(S)
                        .transactedAt(LocalDateTime.now())
                        .build()
                );

        mockMvc.perform(post("/transaction/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UseBalance.Request(1L, "1234567890", 1000L)
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1000000000"))
                .andExpect(jsonPath("$.amount").value(12345L))
                .andExpect(jsonPath("$.transactionId").value("transactionId"))
                .andExpect(jsonPath("$.transactionResultType").value("S"))
                .andDo(print());
    }


    @Test
    void cancelBalanceSuccess() throws Exception {
        given(transactionService.cancelBalance(anyString(), anyString(), anyLong()))
                .willReturn(TransactionDTO.builder()
                        .accountNumber("1000000000")
                        .amount(12345L)
                        .transactionId("transactionId")
                        .transactionResultType(S)
                        .transactedAt(LocalDateTime.now())
                        .build()
                );

        mockMvc.perform(post("/transaction/cancel")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CancelBalance.Request("testTransactionId", "1234567890", 1000L)
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("1000000000"))
                .andExpect(jsonPath("$.amount").value(12345L))
                .andExpect(jsonPath("$.transactionId").value("transactionId"))
                .andExpect(jsonPath("$.transactionResultType").value("S"))
                .andDo(print());
    }


    @Test
    void getTransactionsTest() throws Exception {
        given(transactionService.queryTransaction(anyString()))
                .willReturn(TransactionDTO.builder()
                        .accountNumber("1234567890")
                        .amount(1000L)
                        .transactionId("transactionId")
                        .transactionType(USE)
                        .transactionResultType(S)
                        .transactedAt(LocalDateTime.now())
                        .build()
                );

        mockMvc.perform(get("/transaction/testTransactionId"))
                .andDo(print())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.amount").value("1000"))
                .andExpect(jsonPath("$.transactionId").value("transactionId"))
                .andExpect(jsonPath("$.transactionType").value("USE"))
                .andExpect(jsonPath("$.transactionResultType").value("S"));
    }


    @TestConfiguration
    static class MockConfig {
        @Bean
        public TransactionService transactionService() {
            return Mockito.mock(TransactionService.class);
        }
    }
}