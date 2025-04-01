package com.ian.account.domain;

import com.ian.account.type.TransactionResultType;
import com.ian.account.type.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Transaction extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType; // 거래 종류 (사용/사용 취소)

    @Enumerated(EnumType.STRING)
    private TransactionResultType transactionResultType; // 거래 결과 (성공/실패)

    @ManyToOne
    private Account account; // 계좌 정보 (Join)

    private Long amount; // 거래 금액
    private Long balanceSnapshot; // 거래 후 계좌 잔액
    private String transactionId; // 거래 아이디
    private LocalDateTime transactedAt; // 거래 일시
}
