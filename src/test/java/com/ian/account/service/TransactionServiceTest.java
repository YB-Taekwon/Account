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
import static com.ian.account.type.TransactionType.CANCEL;
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


    ///////////////////////////////////// UseBalance /////////////////////////////////////

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


    ///////////////////////////////////// CancelBalance /////////////////////////////////////

    @Test
    void cancelBalanceSuccess() {
        // given
        AccountUser accountUser = AccountUser.builder()
                .id(12L)
                .userName("Isaiah").build();

        Account account = Account.builder()
                .accountUser(accountUser)
                .accountStatus(AccountStatus.ACTIVE)
                .balance(9000L)
                .accountNumber("1000000012").build();

        Transaction transaction = Transaction.builder()
                .account(account)
                .amount(1000L)
                .balanceSnapshot(9000L)
                .transactionId("transactionId")
                .transactionResultType(S)
                .transactionType(USE)
                .transactedAt(LocalDateTime.now())
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .amount(1000L)
                        .balanceSnapshot(10000L)
                        .transactionId("transactionId")
                        .transactionResultType(S)
                        .transactionType(CANCEL)
                        .transactedAt(LocalDateTime.now())
                        .build()
                );

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

        // when
        TransactionDTO transactionDTO = transactionService
                .cancelBalance("testTransactionId", "1234567890", 1000L);

        // then
        assertEquals(1000L, transactionDTO.getAmount());
        assertEquals(10000L, transactionDTO.getBalanceSnapshot());
        assertEquals("transactionId", transactionDTO.getTransactionId());
        assertEquals(S, transactionDTO.getTransactionResultType());
        assertEquals(CANCEL, transactionDTO.getTransactionType());

        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(1000L, captor.getValue().getAmount());
        assertEquals(10000L, captor.getValue().getBalanceSnapshot());
        assertEquals(S, captor.getValue().getTransactionResultType());
        assertEquals(CANCEL, captor.getValue().getTransactionType());
    }

    @Test
    @DisplayName("잔액 사용 취소 시, 계좌를 찾을 수 없을 때")
    void cancelBalance_accountNotFound() {
        // given
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(Transaction.builder().build()));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());

        // when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService
                        .cancelBalance("testTransactionId", "1111111111", 1000L));

        // then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("잔액 사용 취소 시, 해당 거래가 없을 때")
    void cancelBalance_transactionNotFound() {
        // given
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());

        // when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance("testTransactionId", "1111111111", 1000L));

        // then
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("잔액 사용 취소 시, 거래와 계좌의 정보가 다를 때")
    void cancelBalance_transactionAccountUnMatch() {
        // given
        AccountUser accountUser = AccountUser.builder()
                .id(12L)
                .userName("Isaiah").build();

        Account account1 = Account.builder()
                .accountUser(accountUser)
                .accountStatus(AccountStatus.ACTIVE)
                .balance(9000L)
                .accountNumber("1000000012").build();

        Account account2 = Account.builder()
                .accountUser(accountUser)
                .accountStatus(AccountStatus.ACTIVE)
                .balance(9000L)
                .accountNumber("1000000013")
                .build();

        Transaction transaction = Transaction.builder()
                .account(account1)
                .amount(1000L)
                .balanceSnapshot(9000L)
                .transactionId("transactionId")
                .transactionResultType(S)
                .transactionType(USE)
                .transactedAt(LocalDateTime.now())
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account2));

        // when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService
                        .cancelBalance("testTransactionId", "1111111111", 1000L));

        // then
        assertEquals(ErrorCode.TRANSACTION_ACCOUNT_UNMATCH, accountException.getErrorCode());
    }

    @Test
    @DisplayName("잔액 사용 취소 시, 거래 금액과 취소 금액이 다를 때")
    void cancelBalance_amountMismatch() {
        // given
        AccountUser accountUser = AccountUser.builder()
                .id(12L)
                .userName("Isaiah").build();

        Account account = Account.builder()
                .accountUser(accountUser)
                .accountStatus(AccountStatus.ACTIVE)
                .balance(10000L)
                .accountNumber("1000000012").build();

        Transaction transaction = Transaction.builder()
                .account(account)
                .amount(1000L)
                .balanceSnapshot(9000L)
                .transactionId("transactionId")
                .transactionResultType(S)
                .transactionType(USE)
                .transactedAt(LocalDateTime.now())
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        // when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService
                        .cancelBalance("testTransactionId", "1111111111", 2000L));

        // then
        assertEquals(ErrorCode.AMOUNT_MISMATCH, accountException.getErrorCode());
    }

    @Test
    @DisplayName("잔액 사용 취소 시, 거래 취소 기간이 지났을 때")
    void cancelBalance_tooOldToCancel() {
        // given
        AccountUser accountUser = AccountUser.builder()
                .id(12L)
                .userName("Isaiah").build();

        Account account = Account.builder()
                .accountUser(accountUser)
                .accountStatus(AccountStatus.ACTIVE)
                .balance(10000L)
                .accountNumber("1000000012").build();

        Transaction transaction = Transaction.builder()
                .account(account)
                .amount(1000L)
                .balanceSnapshot(9000L)
                .transactionId("transactionId")
                .transactionResultType(S)
                .transactionType(USE)
                .transactedAt(LocalDateTime.now().minusYears(1).minusDays(1))
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        // when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService
                        .cancelBalance("testTransactionId", "1111111111", 1000L));

        // then
        assertEquals(ErrorCode.TRANSACTION_CANCELLATION_EXPIRED, accountException.getErrorCode());
    }
}