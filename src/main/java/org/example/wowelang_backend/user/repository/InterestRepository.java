package org.example.wowelang_backend.user.repository;

import org.example.wowelang_backend.user.domain.Interest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InterestRepository extends JpaRepository<Interest, Long> {
    Optional<Interest> findByName(String name);
}
