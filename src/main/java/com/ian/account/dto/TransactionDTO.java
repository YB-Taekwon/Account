package com.ian.account.dto;

import com.ian.account.domain.Transaction;
import com.ian.account.type.TransactionResultType;
import com.ian.account.type.TransactionType;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDTO {
    private String accountNumber;
    private TransactionType transactionType; // 거래 종류 (사용/사용 취소)
    private TransactionResultType transactionResultType; // 거래 결과 (성공/실패)
    private Long amount; // 거래 금액
    private Long balanceSnapshot; // 거래 후 계좌 잔액
    private String transactionId; // 거래 아이디
    private LocalDateTime transactedAt; // 거래 일시


    public static TransactionDTO fromEntity(Transaction transaction) {
        return TransactionDTO.builder()
                .accountNumber(transaction.getAccount().getAccountNumber())
                .transactionType(transaction.getTransactionType())
                .transactionResultType(transaction.getTransactionResultType())
                .amount(transaction.getAmount())
                .balanceSnapshot(transaction.getBalanceSnapshot())
                .transactionId(transaction.getTransactionId())
                .transactedAt(transaction.getTransactedAt())
                .build();
    }
}
