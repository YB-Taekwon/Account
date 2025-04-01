package com.ian.account.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DeleteAccount: "계좌 해지"라는 하나의 기능을 담당
 * 계좌 생성을 위해 클라이언트로부터 요청받는 Request와 계좌 생성 후 클라이언트에게 응답하는 Response 모두
 * 계좌 생성 기능에 관련되어 있기 때문에 CreateAccount 클래스의 중첩(static) 클래스로 관리한다.
 * Request: 사용자 아이디, 계좌 번호
 * Response: 사용자 아이디, 계좌 번호, 계좌 해지 일시
 */
public class DeleteAccount {

    @Getter
    @Setter
    @AllArgsConstructor
    public static class Request {
        @NotNull
        @Min(1)
        private Long userId; // 사용자 아이디

        @NotBlank
        @Size(min = 10, max = 10)
        private String accountNumber; // 계좌 번호
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long userId; // 사용자 아이디
        private String accountNumber; // 계좌 번호
        private LocalDateTime accountCancelledAt; // 계좌 해지 일시

        public static Response from(AccountDTO accountDTO) {
            return Response.builder()
                    .userId(accountDTO.getId())
                    .accountNumber(accountDTO.getAccountNumber())
                    .accountCancelledAt(accountDTO.getAccountCancelledAt())
                    .build();
        }
    }
}
