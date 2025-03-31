package com.ian.account.controller;

import com.ian.account.domain.Account;
import com.ian.account.dto.CreateAccount;
import com.ian.account.service.AccountService;
import com.ian.account.service.RedisTestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;
    private final RedisTestService redisTestService;

    @GetMapping("/get-lock")
    public String getLock() {
        return redisTestService.getLock();
    }

    /*
        계좌 생성
        POST /account
        파라미터: 사용자 아이디, 초기 잔액
        실패: 사용자가 업는 경우, 계좌가 10개 인 경우
        성공: 사용자 아이디, 계좌번호, 등록 일시 반환
    */
    @PostMapping("/account")
    public CreateAccount.Response createAccount(@RequestBody @Valid CreateAccount.Request request) {
        return CreateAccount.Response.from(
                accountService.createAccount(
                        request.getUserId(),
                        request.getInitialBalance()
                )
        );
    }


    /*
        계좌 해지
        DELETE /account
        파라미터: 사용자 아이디, 계좌번호
        실패: 사용자 또는 계좌가 없는 경우, 사용자 아이디와 계좌 소유주가 다른 경우,
        계좌가 이미 해지 상태인 경우, 잔액이 있는 경우
        성고이
     */
    @GetMapping("/get-account/{id}")
    public Account getAccount(@PathVariable Long id) {
        return accountService.getAccount(id);
    }
}
