package com.ian.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
    ACCOUNT_LIMIT_EXCEEDED("사용자가 생성할 수 있는 최대 계좌 개수를 초과했습니다.");

    private final String disciption;
}
