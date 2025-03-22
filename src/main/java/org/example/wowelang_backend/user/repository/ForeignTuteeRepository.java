package org.example.wowelang_backend.user.repository;

import org.example.wowelang_backend.user.domain.ForeignTuteeAttribute;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForeignTuteeRepository extends JpaRepository<ForeignTuteeAttribute, Long> {
    Optional<ForeignTuteeAttribute> findByUser_Id(Long userId); // User 엔티티의 pk
}
