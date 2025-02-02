package com.appsolve.wearther_backend.repository;

import com.appsolve.wearther_backend.domain.Member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Member findByLoginId(String loginId);

    Member findByMemberId(Long MemberId);
}
