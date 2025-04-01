package com.ian.account.service;

import com.ian.account.domain.*;
import com.ian.account.dto.AccountDTO;
import com.ian.account.exception.AccountException;
import com.ian.account.repository.*;
import com.ian.account.type.AccountStatus;
import com.ian.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// 단위 테스트
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    // 의존성 mocking
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    // 테스트 대상 (@Mock 애너테이션 의존성 주입)
    @InjectMocks
    private AccountService accountService;


    ///////////////////////////////////// GetAccounts /////////////////////////////////////

    @Test
    void getAccountsSuccess() {
        // given
        AccountUser accountUser = AccountUser.builder()
                .id(12L)
                .userName("Isaiah").build();

        List<Account> accounts = Arrays.asList(
                Account.builder().accountUser(accountUser).accountNumber("1111111111").balance(1000L).build(),
                Account.builder().accountUser(accountUser).accountNumber("2222222222").balance(2000L).build(),
                Account.builder().accountUser(accountUser).accountNumber("3333333333").balance(3000L).build()
        );

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findByAccountUser(any()))
                .willReturn(accounts);

        // when
        List<AccountDTO> accountDTOList = accountService.getAccountsByUserId(1L);

        // then
        assertEquals(3, accountDTOList.size());
        assertEquals("1111111111", accountDTOList.get(0).getAccountNumber());
        assertEquals(1000, accountDTOList.get(0).getBalance());
        assertEquals("2222222222", accountDTOList.get(1).getAccountNumber());
        assertEquals(2000, accountDTOList.get(1).getBalance());
        assertEquals("3333333333", accountDTOList.get(2).getAccountNumber());
        assertEquals(3000, accountDTOList.get(2).getBalance());
    }

    @Test
    @DisplayName("계좌 조회 시, 사용자가 없을 때")
    void getAccountsFailed() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.getAccountsByUserId(1L));

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
    }


    ///////////////////////////////////// CreateAccount /////////////////////////////////////

    @Test
    void createAccountSuccess() {
        // given
        AccountUser accountUser = AccountUser.builder()
                .id(12L)
                .userName("Isaiah").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.of(Account.builder()
                        .accountUser(accountUser)
                        .accountNumber("1000000012").build()));

        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(accountUser)
                        .accountNumber("1000000013").build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // when
        AccountDTO accountDTO = accountService.createAccount(1L, 1000L);

        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDTO.getId());
        assertEquals("1000000013", captor.getValue().getAccountNumber());
    }

    @Test
    @DisplayName("첫 계좌 번호 생성")
    void createFirstAccount() {
        // given
        AccountUser accountUser = AccountUser.builder()
                .id(15L)
                .userName("Isaiah").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.empty());

        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(accountUser)
                        .accountNumber("1000000013").build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        // when
        AccountDTO accountDTO = accountService.createAccount(1L, 1000L);

        // then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(15L, accountDTO.getId());
        assertEquals("1000000000", captor.getValue().getAccountNumber());
    }

    @Test
    @DisplayName("계좌 생성 시, 사용자를 찾을 수 없을 때")
    void createAccount_userNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 1000L));

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("사용자 최대 계좌 수는 10개")
    void createAccount_maxAccount() {
        // given
        AccountUser accountUser = AccountUser.builder()
                .id(15L)
                .userName("Isaiah").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.countByAccountUser(any()))
                .willReturn(10);

        // when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 1000L));

        // then
        assertEquals(ErrorCode.ACCOUNT_LIMIT_EXCEEDED, accountException.getErrorCode());
    }


    ///////////////////////////////////// DeleteAccount /////////////////////////////////////

    @Test
    void deleteAccountSuccess() {
        // given
        AccountUser accountUser = AccountUser.builder()
                .id(12L)
                .userName("Isaiah").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(accountUser)
                        .balance(0L)
                        .accountNumber("1000000012").build()));

        // when
        AccountDTO accountDTO = accountService.deleteAccount(1L, "1111111111");

        // then
        assertEquals(12L, accountDTO.getId());
        assertEquals("1000000012", accountDTO.getAccountNumber());
    }


    @Test
    @DisplayName("계좌 해지 시, 사용자를 찾을 수 없을 때")
    void deleteAccount_userNotFound() {
        // given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

        // when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1111111111"));

        // then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("계좌 해지 시, 계좌를 찾을 수 없을 때")
    void deleteAccount_accountNotFound() {
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
                () -> accountService.deleteAccount(1L, "1111111111"));

        // then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("계좌 해지 시, 계좌의 소유주가 다를 때")
    void deleteAccount_userUnMatch() {
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
                () -> accountService.deleteAccount(1L, "1111111111"));

        // then
        assertEquals(ErrorCode.USER_ACCOUNT_UNMATCH, accountException.getErrorCode());
    }

    @Test
    @DisplayName("계좌 해지 시, 계좌에 잔액이 남아있을 때")
    void deleteAccount_accountHasBalance() {
        // given
        AccountUser accountUser = AccountUser.builder()
                .id(15L)
                .userName("Isaiah").build();

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(accountUser));

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(accountUser)
                        .balance(1000L)
                        .accountNumber("1234567890")
                        .build()));

        // when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1111111111"));

        // then
        assertEquals(ErrorCode.ACCOUNT_HAS_BALANCE, accountException.getErrorCode());
    }

    @Test
    @DisplayName("계좌 해지 시, 이미 해지가 된 상태일 때")
    void deleteAccount_accountAlreadyClosed() {
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
                () -> accountService.deleteAccount(1L, "1111111111"));

        // then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_CLOSED, accountException.getErrorCode());
    }

}