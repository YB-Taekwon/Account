package com.ian.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ian.account.dto.AccountDTO;
import com.ian.account.dto.CreateAccount;
import com.ian.account.dto.DeleteAccount;
import com.ian.account.service.AccountService;
import com.ian.account.service.RedisTestService;
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

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// 컨트롤러 슬라이스 테스트
@WebMvcTest(AccountController.class)
@Import(AccountControllerTest.MockConfig.class) // mock bean 수동 등록
class AccountControllerTest {
    // Spring Boot 3.4.0 부터 MockBean 사용 불가
    @Autowired
    private AccountService accountService;

    @Autowired
    private RedisTestService redisTestService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    void createAccountTest() throws Exception {
        // given + willReturn: 테스트 중 어떠한 메서드를 호출했을 때, 반환할 값을 사전에 설정
        // AccountService의 createAccount 메서드 호출 시, builder로 생성한 AccountDTO 객체 반환
        given(accountService.createAccount(anyLong(), anyLong()))
                .willReturn(AccountDTO.builder()
                        .id(1L)
                        .accountNumber("1234567890")
                        .accountCreatedAt(LocalDateTime.now())
                        .accountCancelledAt(LocalDateTime.now())
                        .build());


        mockMvc.perform(post("/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CreateAccount.Request(1L, 1000L)
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andDo(print());
    }

    @Test
    void deleteAccountTest() throws Exception {
        given(accountService.deleteAccount(anyLong(), anyString()))
                .willReturn(AccountDTO.builder()
                        .id(1L)
                        .accountNumber("1234567890")
                        .accountCreatedAt(LocalDateTime.now())
                        .accountCancelledAt(LocalDateTime.now())
                        .build());


        mockMvc.perform(delete("/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new DeleteAccount.Request(1L, "1111111111")
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andDo(print());
    }

    // mock bean 수동 등록
    @TestConfiguration
    static class MockConfig {
        @Bean
        public AccountService accountService() {
            return Mockito.mock(AccountService.class);
        }

        @Bean
        public RedisTestService redisTestService() {
            return Mockito.mock(RedisTestService.class);
        }
    }

}