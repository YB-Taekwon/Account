package com.ian.account.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

/**
 * CreateAccount: "계좌 생성"이라는 하나의 기능을 담당
 * 계좌 생성을 위해 클라이언트로부터 요청받는 Request와 계좌 생성 후 클라이언트에게 응답하는 Response 모두
 * 계좌 생성 기능에 관련되어 있기 때문에 CreateAccount 클래스의 중첩(static) 클래스로 관리한다.
 * Request: 사용자 아이디, 초기 잔액
 * Response: 사용자 아이디, 계좌번호, 계좌 생성 일시
 */
public class CreateAccount {

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Request {
        @NotNull
        @Min(1)
        private Long userId; // 사용자 아이디

        @NotNull
        @Min(0)
        private Long initialBalance; // 초기 잔액
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long userId; // 사용자 아이디
        private String accountNumber; // 계좌 번호
        private LocalDateTime accountCreatedAt; // 계좌 생성 일시

        public static Response from(AccountDTO accountDTO) {
            return Response.builder()
                    .userId(accountDTO.getId())
                    .accountNumber(accountDTO.getAccountNumber())
                    .accountCreatedAt(accountDTO.getAccountCreatedAt())
                    .build();
        }
    }
}
