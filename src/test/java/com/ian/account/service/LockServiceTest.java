package com.ian.account.service;

import com.ian.account.exception.AccountException;
import com.ian.account.type.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import static com.ian.account.type.ErrorCode.ACCOUNT_TRANSACTION_LOCK;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LockServiceTest {
    @Mock
    private RedissonClient redisson;

    @Mock
    private RLock lock;

    @InjectMocks
    private LockService lockService;


    @Test
    void getLockSuccess() throws InterruptedException {
        // given
        given(redisson.getLock(anyString()))
                .willReturn(lock);
        given(lock.tryLock(anyLong(), anyLong(), any()))
                .willReturn(true);

        // when
        // then
        assertDoesNotThrow(() -> lockService.lock("1234"));
    }

    @Test
    void getLockFailed() throws InterruptedException {
        // given
        given(redisson.getLock(anyString()))
                .willReturn(lock);
        given(lock.tryLock(anyLong(), anyLong(), any()))
                .willReturn(false);

        // when
        AccountException accountException = assertThrows(AccountException.class,
                () -> lockService.lock("1234"));
        // then
        assertEquals(ACCOUNT_TRANSACTION_LOCK, accountException.getErrorCode());
    }

}