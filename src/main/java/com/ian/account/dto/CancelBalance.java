package com.ian.account.dto;

import com.ian.account.aop.AccountLockIdInterface;
import com.ian.account.type.TransactionResultType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * CancelBalance: "잔액 사용 취소"라는 하나의 기능을 담당
 * 계좌 생성을 위해 클라이언트로부터 요청받는 Request와 계좌 생성 후 클라이언트에게 응답하는 Response 모두
 * 계좌 생성 기능에 관련되어 있기 때문에 CreateAccount 클래스의 중첩(static) 클래스로 관리한다.
 * Request: 거래 아이디, 계좌 번호, 취소 요청 금액
 * Response: 계좌 번호, 거래 결과 코드(성공/실패), 거래 아이디, 거래 금액, 거래 일시
 */
public class CancelBalance {
    @Getter
    @Setter
    @AllArgsConstructor
    public static class Request implements AccountLockIdInterface {
        @NotBlank
        private String transactionId; // 거래 아이디

        @NotBlank
        @Size(min = 10, max = 10)
        private String accountNumber; // 계좌 번호

        @NotNull
        @Min(10)
        @Max(10_0000_0000)
        private Long amount; // 취소 요청 금액
    }


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private String accountNumber; // 계좌 번호
        private TransactionResultType transactionResultType; // 거래 결과 코드
        private String transactionId; // 거래 아이디
        private Long amount; // 거래 금액
        private LocalDateTime transactedAt; // 거래 일시

        public static Response from(TransactionDTO transactionDTO) {
            return Response.builder()
                    .accountNumber(transactionDTO.getAccountNumber())
                    .transactionResultType(transactionDTO.getTransactionResultType())
                    .transactionId(transactionDTO.getTransactionId())
                    .amount(transactionDTO.getAmount())
                    .transactedAt(transactionDTO.getTransactedAt())
                    .build();
        }
    }
}
