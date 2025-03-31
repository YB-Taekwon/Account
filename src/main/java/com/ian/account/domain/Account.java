package com.ian.account.domain;

import com.ian.account.type.AccounStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
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
@Builder
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Account {
    @Id
    @GeneratedValue
    private Long id; // 계좌 아이디

    @ManyToOne
    private AccountUser accountUser; // 사용자 정보(Join)

    @Column(unique = true)
    private String accountNumber; // 계좌 번호

    @Enumerated(EnumType.STRING)
    private AccounStatus accounStatus; // 계좌 상태 (사용/해지)

    private Long balance; // 잔액
    private LocalDateTime accountCreatedAt; // 계좌 등록 일시
    private LocalDateTime accountCancelledAt; // 계좌 해지 일시

    @CreatedDate
    private LocalDateTime registeredAt; // 생성 일시

    @LastModifiedDate
    private LocalDateTime updatedAt; // 수정 일시
}
