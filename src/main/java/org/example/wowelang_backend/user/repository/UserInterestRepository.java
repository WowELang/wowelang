package org.example.wowelang_backend.user.repository;

import org.example.wowelang_backend.user.domain.UserInterest;
import org.example.wowelang_backend.user.domain.UserInterestId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface UserInterestRepository extends JpaRepository<UserInterest, UserInterestId> {
    List<UserInterest> findAllByUserId(Long userId);
    void deleteByUserIdAndInterestIdIn(Long userId, Collection<Long> interestIds);
}
