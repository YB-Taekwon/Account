package com.ian.account.service;

import com.ian.account.domain.Account;
import com.ian.account.domain.AccountUser;
import com.ian.account.dto.AccountDTO;
import com.ian.account.exception.AccountException;
import com.ian.account.repository.AccountRepository;
import com.ian.account.repository.AccountUserRepository;
import com.ian.account.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.ian.account.type.AccounStatus.ACTIVE;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;

    @Transactional
    public AccountDTO createAccount(Long userId, Long initialBalance) {
        // 1-1. 사용자 등록 여부 확인
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));

        // 1-2. 사용자 계좌 수 확인
        validateCreateAccount(accountUser);

        // 2. 계좌 번호 생성
        String newAccountNumber = accountRepository.findFirstByOrderByIdDesc()
                .map(account -> (Integer.parseInt(account.getAccountNumber())) + 1 + "")
                .orElse("1000000000");

        // 3. 계좌 번호 저장 및 정보 반환
        return AccountDTO.fromEntity(accountRepository.save(
                        Account.builder()
                                .accountUser(accountUser)
                                .accounStatus(ACTIVE)
                                .accountNumber(newAccountNumber)
                                .balance(initialBalance)
                                .accountCreatedAt(LocalDateTime.now())
                                .build()
                )
        );
    }

    // 사용자 계좌 수 확인
    private void validateCreateAccount(AccountUser accountUser) {
        if (accountRepository.countByAccountUser(accountUser) >= 10) {
            throw new AccountException(ErrorCode.ACCOUNT_LIMIT_EXCEEDED);
        }
    }


    // 계좌 번호 조회
    @Transactional
    public Account getAccount(Long id) {
        return accountRepository.findById(id).get();
    }
}
