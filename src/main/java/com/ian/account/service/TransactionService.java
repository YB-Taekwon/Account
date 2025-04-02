package com.ian.account.service;


import com.ian.account.domain.Account;
import com.ian.account.domain.AccountUser;
import com.ian.account.domain.Transaction;
import com.ian.account.dto.TransactionDTO;
import com.ian.account.exception.AccountException;
import com.ian.account.repository.AccountRepository;
import com.ian.account.repository.AccountUserRepository;
import com.ian.account.repository.TransactionRepository;
import com.ian.account.type.AccountStatus;
import com.ian.account.type.TransactionResultType;
import com.ian.account.type.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import static com.ian.account.type.ErrorCode.*;
import static com.ian.account.type.TransactionResultType.F;
import static com.ian.account.type.TransactionResultType.S;
import static com.ian.account.type.TransactionType.CANCEL;
import static com.ian.account.type.TransactionType.USE;

@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;

    /**
     * 실패 케이스
     * 1. 사용자가와 계좌가 없는 경우
     * 2. 사용자와 계좌의 소유주 정보가 일치하지 않는 경우
     * 3. 계좌가 해지 상태인 경우
     * 4. 거래 금액이 잔액보다 큰 경우
     * 5. 해당 계좌에서 거래(사용, 사용 취소)가 이미 진행 중인 경우 (다른 거래 요청이 오는 경우, 해당 거래가 동시에 잘못 처리되는 것을 방지)
     * 거래 금액이 너무 작거나 큰 경우 -> Entity: @Min, @Max + Controller: @Valid 애너테이션으로 유효성 검사 완료
     */
    // 잔액 사용
    @Transactional
    public TransactionDTO useBalance(Long userId, String accountNumber, Long amount) {
        // 1-1. 사용자가 없는 경우 예외 발생
        AccountUser accountUser = accountUserRepository.findById(userId).orElseThrow(
                () -> new AccountException(USER_NOT_FOUND));
        // 1-2. 계좌가 없는 경우 예외 발생
        Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(
                () -> new AccountException(ACCOUNT_NOT_FOUND));
        // 나머지 유효성 검사
        validateUseBalance(accountUser, account, amount);

        // 거래 후, 계좌에 남은 잔액 갱신
        account.useBalance(amount);

        return saveAndGetTransaction(USE, S, account, amount);
    }

    // 잔액 사용 - 유효성 검사
    private void validateUseBalance(AccountUser accountUser, Account account, Long amount) {
        // 2. 사용자와 계좌 소유주의 정보가 일치하지 않을 경우 예외 발생
        if (!Objects.equals(accountUser.getId(), account.getAccountUser().getId()))
            throw new AccountException(USER_ACCOUNT_UNMATCH);
        // 3. 계좌가 해지 상태인 경우 예외 발생
        if (account.getAccountStatus() != AccountStatus.ACTIVE)
            throw new AccountException(ACCOUNT_ALREADY_CLOSED);
        // 4. 거래 금액이 잔액보다 큰 경우 예외 발생
        if (account.getBalance() < amount)
            throw new AccountException(BALANCE_EXCEEDED);
        // 5. 해당 계좌에서 거래(사용, 사용 취소)가 이미 진행 중인 경우 예외 발생 -> 동시성 제어
    }

    // 잔액 사용에 실패한 경우에도 거래 내역을 기록
    @Transactional
    public void saveFailedUseTransaction(String accountNumber, Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        saveAndGetTransaction(USE, F, account, amount);
    }


    /**
     * 실패 케이스
     * 1. 거래 아이디에 해당하는 거래가 없는 경우
     * 2. 계좌가 없는 경우
     * 3. 거래와 계좌가 일치하지 않는 경우
     * 4. 거래 금액과 거래 취소 금액이 다른 경우
     * 5. 거래 기간이 1년을 넘은 경우
     * 6. 해당 계좌에서 이미 다른 거래(사용/사용 취소)를 진행 중인 경우 (다른 거래 요청이 오는 경우, 해당 거래가 동시에 잘못 처리되는 것을 방지)
     */
    // 잔액 사용 취소
    @Transactional
    public TransactionDTO cancelBalance(String transactionId, String accountNumber, Long amount) {
        // 1. 거래 아이디에 해당하는 거래가 없는 경우 예외 발생
        Transaction transaction = transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new AccountException(TRANSACTION_NOT_FOUND));
        // 2. 계좌가 없는 경우 예외 발생
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));
        // 나머지 유효성 검사
        validateCancelBalance(transaction, account, amount);

        // 거래 취소 후, 계좌에 남은 잔액 갱신
        account.cancelBalance(amount);

        return saveAndGetTransaction(CANCEL, S, account, amount);
    }

    private void validateCancelBalance(Transaction transaction, Account account, Long amount) {
        // 3. 거래와 계좌가 일치하지 않는 경우
        if (!Objects.equals(transaction.getAccount().getAccountNumber(), account.getAccountNumber()))
            throw new AccountException(TRANSACTION_ACCOUNT_UNMATCH);
        // 4. 거래 금액과 거래 취소 금액이 다른 경우
        if (!Objects.equals(transaction.getAmount(), amount))
            throw new AccountException(AMOUNT_MISMATCH);
        // 5. 거래 기간이 1년을 넘은 경우
        if (transaction.getTransactedAt().isBefore(LocalDateTime.now().minusYears(1)))
            throw new AccountException(TRANSACTION_CANCELLATION_EXPIRED);
        // 6. 해당 계좌에서 이미 다른 거래(사용/사용 취소)를 진행 중인 경우 (다른 거래 요청이 오는 경우, 해당 거래가 동시에 잘못 처리되는 것을 방지)
    }

    // 잔액 사용 취소에 실패한 경우에도 거래 내역을 기록
    @Transactional
    public void saveFailedCancelTransaction(String accountNumber, Long amount) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        saveAndGetTransaction(CANCEL, F, account, amount);
    }


    // 거래 정보를 저장한 후, DTO로 변환하여 반환
    private TransactionDTO saveAndGetTransaction(
            TransactionType transactionType, TransactionResultType transactionResultType, Account account, Long amount) {
        return TransactionDTO.fromEntity(transactionRepository.save(
                Transaction.builder()
                        .account(account)
                        .amount(amount)
                        .balanceSnapshot(account.getBalance())
                        .transactionType(transactionType)
                        .transactionResultType(transactionResultType)
                        .transactionId(UUID.randomUUID().toString().replace("-", ""))
                        .transactedAt(LocalDateTime.now())
                        .build()
        ));
    }


    /**
     * 실패 케이스
     * 거래 아이디에 해당하는 거래가 없는 경우
     */
    // 잔액 사용 확인
    @Transactional
    public TransactionDTO queryTransaction(String transactionId) {
        // 거래 아이디에 해당하는 거래가 없는 경우 예외 발생
        return TransactionDTO.fromEntity(transactionRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new AccountException(TRANSACTION_NOT_FOUND)));
    }
}
