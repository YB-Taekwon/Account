package com.ian.account.domain;

import com.ian.account.exception.AccountException;
import com.ian.account.type.AccountStatus;
import com.ian.account.type.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 *  Account: "계좌 정보"를 담고 있는 Entity 객체
 *
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Account extends BaseEntity {
    @ManyToOne
    private AccountUser accountUser; // 사용자 정보 (Join)

    @Column(unique = true)
    private String accountNumber; // 계좌 번호

    @Enumerated(EnumType.STRING)
    private AccountStatus accountStatus; // 계좌 상태 (사용/해지)

    private Long balance; // 잔액
    private LocalDateTime accountCreatedAt; // 계좌 생성 일시
    private LocalDateTime accountCancelledAt; // 계좌 해지 일시


    // 잔액 사용 시, 남은 잔액 계산
    public void useBalance(Long amount) {
        if (amount > balance)
            throw new AccountException(ErrorCode.BALANCE_EXCEEDED);

        balance -= amount;
    }
}
