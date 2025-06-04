package org.example.wowelang_backend.user.repository;

import org.example.wowelang_backend.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByLoginId(String loginId);

    boolean existsByEmail(String email);

    boolean existsByLoginId(String loginId);

    // 논리 삭제된 유저를 제외하고 찾기
    @Query("SELECT u FROM User u WHERE u.id = :id AND u.isDelete = false")
    Optional<User> findActiveById(@Param("id") Long id);

    @Query("SELECT u FROM User u WHERE u.loginId = :loginId AND u.isDelete = false")
    Optional<User> findActiveByLoginId(@Param("loginId") String loginId);
}
