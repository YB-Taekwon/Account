package com.ian.account.dto;

import com.ian.account.domain.Account;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity는 DB와 직접 연결되어 있는 객체이기 때문에
 * 데이터 교환을 위해 필요한 필드만 담은 DTO를 새로 생성하여 역할을 분리
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDTO {
    private Long id;
    private String accountNumber;
    private Long balance;
    private LocalDateTime accountCreatedAt;
    private LocalDateTime accountCancelledAt;

    // Entity -> DTO 변환 팩토리 메서드
    public static AccountDTO fromEntity(Account account) {
        return AccountDTO.builder()
                .id(account.getAccountUser().getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .accountCreatedAt(account.getAccountCreatedAt())
                .accountCancelledAt(account.getAccountCancelledAt())
                .build();
    }
}
