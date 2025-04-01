package com.ian.account.service;

import com.ian.account.domain.*;
import com.ian.account.dto.AccountDTO;
import com.ian.account.exception.AccountException;
import com.ian.account.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.ian.account.type.AccountStatus.ACTIVE;
import static com.ian.account.type.AccountStatus.CLOSED;
import static com.ian.account.type.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;


    // 계좌 확인
    @Transactional
    public List<AccountDTO> getAccountsByUserId(Long id) {
        AccountUser accountUser = accountUserRepository.findById(id)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));

        List<Account> accounts = accountRepository.findByAccountUser(accountUser);

        return accounts.stream()
                .map(AccountDTO::fromEntity)
                .toList();
    }


    // 계좌 생성
    @Transactional
    public AccountDTO createAccount(Long userId, Long initialBalance) {
        // 1-1. 사용자 등록 여부 확인
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));

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
                                .accountStatus(ACTIVE)
                                .accountNumber(newAccountNumber)
                                .balance(initialBalance)
                                .accountCreatedAt(LocalDateTime.now())
                                .build()
                )
        );
    }

    // 계좌 생성 - 유효성 검사
    private void validateCreateAccount(AccountUser accountUser) {
        // 계좌를 10개 이상 보유하고 있을 경우 예외 발생 -> 생성 가능한 계좌의 최대 개수: 10
        if (accountRepository.countByAccountUser(accountUser) >= 10) {
            throw new AccountException(ACCOUNT_LIMIT_EXCEEDED);
        }
    }


    // 계좌 해지
    @Transactional
    public AccountDTO deleteAccount(Long userId, String accountNumber) {
        AccountUser accountUser = accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        validateDeleteAccount(accountUser, account);

        account.setAccountStatus(CLOSED);
        account.setAccountCancelledAt(LocalDateTime.now());

        return AccountDTO.fromEntity(account);
    }

    // 계좌 해지 - 유효성 검사
    private void validateDeleteAccount(AccountUser accountUser, Account account) {
        // 사용자와 계좌 소유주의 정보가 일치하지 않을 경우 예외 발생
        if (accountUser.getId() != account.getAccountUser().getId()) {
            throw new AccountException(USER_ACCOUNT_UNMATCH);
        }
        // 계좌가 이미 해지된 상태일 경우 예외 발생
        if (account.getAccountStatus() == CLOSED) {
            throw new AccountException(ACCOUNT_ALREADY_CLOSED);
        }
        // 계좌에 잔액이 남아있는 경우 예외 발생
        if (account.getBalance() > 0) {
            throw new AccountException(ACCOUNT_HAS_BALANCE);
        }
    }

}
