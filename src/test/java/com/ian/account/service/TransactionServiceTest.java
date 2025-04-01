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
import com.ian.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.ian.account.type.ErrorCode.BALANCE_EXCEEDED;
import static com.ian.account.type.TransactionResultType.F;
import static com.ian.account.type.TransactionResultType.S;
import static com.ian.account.type.TransactionType.USE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void useBalanceSuccess() {
        // given
        AccountUser accountUser = AccountUser.builder()
                .id(12L)
                .userName("Isaiah").build();

        Account account = Account.builder()
                .accountUser(accountUser)
                .accountStatus(AccountStatus.ACTIVE)
                .balance(10000L)
                .accountNumber("1000000012").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .amount(1000L)
                        .balanceSnapshot(9000L)
                        .transactionResultType(S)
                        .transactionType(USE)
                        .transactedAt(LocalDateTime.now())
                        .build()
                );

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        // when
        TransactionDTO transactionDTO = transactionService.useBalance(1L, "1234567890", 1234L);

        // then
        assertEquals(1000L, transactionDTO.getAmount());
        assertEquals(9000L, transactionDTO.getBalanceSnapshot());
        assertEquals(S, transactionDTO.getTransactionResultType());
        assertEquals(USE, transactionDTO.getTransactionType());

        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(1234L, captor.getValue().getAmount());
        assertEquals(8766L, captor.getValue().getBalanceSnapshot());
    }

    @Test
    @DisplayName("잔액 사용 시, 사용자를 찾을 수 없을 때")
    void useBalance_userNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1000000000", 1000L));

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("잔액 사용 시, 계좌를 찾을 수 없을 때")
    void useBalance_accountNotFound() {
        // given
        AccountUser accountUser = AccountUser.builder()
                .id(15L)
                .userName("Isaiah").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        // when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1111111111", 1000L));

        // then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("잔액 사용 시, 계좌의 소유주가 다를 때")
    void useBalance_userUnMatch() {
        // given
        AccountUser isaiah = AccountUser.builder()
                .id(15L)
                .userName("Isaiah").build();

        AccountUser peter = AccountUser.builder()
                .id(16L)
                .userName("Peter").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(isaiah));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(peter)
                        .balance(0L)
                        .accountNumber("1234567890")
                        .build()));

        // when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1111111111", 1000L));

        // then
        assertEquals(ErrorCode.USER_ACCOUNT_UNMATCH, accountException.getErrorCode());
    }

    @Test
    @DisplayName("잔액 사용 시, 계좌가 이미 해지가 된 상태일 때")
    void useBalance_accountAlreadyClosed() {
        // given
        AccountUser accountUser = AccountUser.builder()
                .id(15L)
                .userName("Isaiah").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(accountUser)
                        .balance(0L)
                        .accountNumber("1234567890")
                        .accountStatus(AccountStatus.CLOSED)
                        .build()));

        // when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1111111111", 1000L));

        // then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_CLOSED, accountException.getErrorCode());
    }

    @Test
    @DisplayName("잔액 사용 시, 거래 금액이 잔액보다 클 때")
    void useBalance_exceedBalance() {
        // given
        AccountUser accountUser = AccountUser.builder()
                .id(12L)
                .userName("Isaiah").build();

        Account account = Account.builder()
                .accountUser(accountUser)
                .accountStatus(AccountStatus.ACTIVE)
                .balance(100L)
                .accountNumber("1000000012").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        // when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1234567890", 1000L));

        // then
        assertEquals(BALANCE_EXCEEDED, accountException.getErrorCode());
    }


    @Test
    void saveFailedUseTransaction() {
        // given
        AccountUser accountUser = AccountUser.builder()
                .id(12L)
                .userName("Isaiah").build();

        Account account = Account.builder()
                .accountUser(accountUser)
                .accountStatus(AccountStatus.ACTIVE)
                .balance(10000L)
                .accountNumber("1000000012").build();

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .amount(1000L)
                        .balanceSnapshot(9000L)
                        .transactionResultType(S)
                        .transactionType(USE)
                        .transactedAt(LocalDateTime.now())
                        .build()
                );

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        // when
        transactionService.saveFailedUseTransaction("1234567890", 1000L);

        // then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(1000L, captor.getValue().getAmount());
        assertEquals(10000L, captor.getValue().getBalanceSnapshot());
        assertEquals(F, captor.getValue().getTransactionResultType());
    }

}