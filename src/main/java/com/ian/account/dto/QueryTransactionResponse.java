package com.ian.account.dto;

import com.ian.account.type.TransactionResultType;
import com.ian.account.type.TransactionType;
import lombok.*;

import java.time.LocalDateTime;


/**
 * QueryTransactionResponse: "잔액 사용 확인"이라는 하나의 기능을 담당
 * Request: 거래 아이디
 * Response: 계좌 번호, 거래 종류(사용/사용 취소), 거래 결과 코드(성공/실패), 거래 아이디, 거래 금액, 거래 일시 반환 (실패한 거래도 반환)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QueryTransactionResponse {
    private String accountNumber; // 계좌 번호
    private TransactionType transactionType; // 거래 종류
    private TransactionResultType transactionResultType; // 거래 결과 코드
    private String transactionId; // 거래 아이디
    private Long amount; // 거래 금액
    private LocalDateTime transactedAt; // 거래 일시

    public static QueryTransactionResponse from(TransactionDTO transactionDTO) {
        return QueryTransactionResponse.builder()
                .accountNumber(transactionDTO.getAccountNumber())
                .transactionType(transactionDTO.getTransactionType())
                .transactionResultType(transactionDTO.getTransactionResultType())
                .transactionId(transactionDTO.getTransactionId())
                .amount(transactionDTO.getAmount())
                .transactedAt(transactionDTO.getTransactedAt())
                .build();
    }
}
