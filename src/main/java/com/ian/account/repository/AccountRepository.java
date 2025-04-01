package com.ian.account.repository;

import com.ian.account.domain.Account;
import com.ian.account.domain.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    // 마지막으로 생성된 계좌 번호 조회
    Optional<Account> findFirstByOrderByIdDesc();
    // 특정 사람이 갖고 있는 계좌의 수 조회
    Integer countByAccountUser(AccountUser accountUser);
    // 계좌 번호 조회
    Optional<Account> findByAccountNumber(String accountNumber);
    //
    List<Account> findByAccountUser(AccountUser accountUser);
}
