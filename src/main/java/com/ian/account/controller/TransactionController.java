package com.ian.account.controller;

import com.ian.account.dto.CancelBalance;
import com.ian.account.dto.UseBalance;
import com.ian.account.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


/**
 * 잔액 관련 컨트롤러
 * 1. 잔액 사용
 * 2. 잔액 사용 취소
 * 3. 거래 확인
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;


    /**
     * 잔액 사용 API
     * POST /transaction/use
     * 파라미터: 사용자 아이디, 계좌 번호, 거래 금액
     * 실패: 사용자와 계좌가 없는 경우, 사용자와 계좌의 소유주 정보가 일치하지 않는 경우,
     * 계좌가 해지 상태인 경우, 거래 금액이 잔액보다 큰 경우, 거래 금액이 너무 작거나 큰 경우
     * 해당 계좌에서 거래(사용, 사용 취소)가 이미 진행 중인 경우 (다른 거래 요청이 오는 경우, 해당 거래가 동시에 잘못 처리되는 것을 방지)
     * 성공: 계좌 번호, 거래 결과 코드(성공/실패), 거래 아이디, 거래 금액, 거래 일시 반환
     */
    @PostMapping("/transaction/use")
    public UseBalance.Response useBalance(@RequestBody @Valid UseBalance.Request request) {
        // 잔액 사용에 성공했을 경우
        try {
            return UseBalance.Response.from(transactionService.useBalance(
                    request.getUserId(), request.getAccountNumber(), request.getAmount())
            );
        } catch (Exception e) {
            // 잔액 사용에 실패했을 경우에도 거래 내역을 기록
            log.error("Failed to use balance ", e);

            transactionService.saveFailedUseTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );
            throw e;
        }
    }

}
