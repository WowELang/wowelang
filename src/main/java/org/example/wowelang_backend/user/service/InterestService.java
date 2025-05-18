package org.example.wowelang_backend.user.service;

import lombok.RequiredArgsConstructor;
import org.example.wowelang_backend.common.apiPayLoad.status.ErrorStatus;
import org.example.wowelang_backend.user.domain.Interest;
import org.example.wowelang_backend.user.domain.User;
import org.example.wowelang_backend.user.domain.UserInterest;
import org.example.wowelang_backend.user.dto.InterestDto;
import org.example.wowelang_backend.user.repository.InterestRepository;
import org.example.wowelang_backend.user.repository.UserInterestRepository;
import org.example.wowelang_backend.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InterestService {

    private final InterestRepository interestRepository;
    private final UserInterestRepository userInterestRepository;
    private final UserRepository userRepository;

    //최초 관심사 세팅
    //이미 init 되어 있으면 400 예외 발생
    public void initInterest(Long userId, List<Long> interestIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        if (user.getInterestInitialized()) {
            throw new IllegalStateException(ErrorStatus.INTERESTS_ALREADY_IVITIALIZED.getMessage());
        }

        // 유효한 Interest 엔티티만 조회
        List<Interest> interests = interestRepository.findAllById(interestIds);
        if (interests.size() != interestIds.size()) {
            throw new IllegalArgumentException(ErrorStatus.INVALID_INTEREST_ID.getMessage());
        }

        // 조인 엔티티 저장
        List<UserInterest> links = interests.stream()
                .map(i -> new UserInterest(user, i))
                .toList();
        userInterestRepository.saveAll(links);

        // 최초 설정 완료 플래그 변경
        user.setInterestInitialized(true);
        userRepository.save(user);
    }

    //전체 관심사 목록 조회
    public List<InterestDto> getAllInterest() {
        return interestRepository.findAll().stream()
                .map(i -> new InterestDto(i.getId(), i.getName()))
                .toList();
    }


    //관심사 수정
    public void updateInterests(Long userId, List<Long> newInterestIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        if (!user.getInterestInitialized()) {
            throw new IllegalStateException(ErrorStatus.INTERESTS_NOT_IVITIALIZED.getMessage());
        }

        // 1) 기존 링크 조회
        List<UserInterest> existing = userInterestRepository.findAllByUserId(userId);
        Set<Long> existingIds = existing.stream()
                .map(ui -> ui.getInterest().getId())
                .collect(Collectors.toSet());

        // 2) 요청받은 ID 집합
        Set<Long> newIds = new HashSet<>(newInterestIds);

        // 3) 삭제할 ID = existing \ new
        Set<Long> toRemove = new HashSet<>(existingIds);
        toRemove.removeAll(newIds);

        // 4) 추가할 ID = new \ existing
        Set<Long> toAdd = new HashSet<>(newIds);
        toAdd.removeAll(existingIds);

        // 5) 삭제
        if (!toRemove.isEmpty()) {
            userInterestRepository.deleteByUserIdAndInterestIdIn(userId, toRemove);
        }

        // 6) 추가
        if (!toAdd.isEmpty()) {
            List<Interest> interestsToAdd = interestRepository.findAllById(toAdd);
            if (interestsToAdd.size() != toAdd.size()) {
                throw new IllegalArgumentException(ErrorStatus.INVALID_INTEREST_ID.getMessage());
            }
            List<UserInterest> linksToAdd = interestsToAdd.stream()
                    .map(i -> new UserInterest(user, i))
                    .toList();
            userInterestRepository.saveAll(linksToAdd);
        }
    }

    //관심사 저장
    private void saveUserInterests(User user, List<Long> interestIds) {
        if (interestIds == null || interestIds.isEmpty()) return;
        var interests = interestRepository.findAllById(interestIds);

        if (interests.size() != interestIds.size()) {
            throw new IllegalStateException(ErrorStatus.INVALID_INTEREST_ID.getMessage());
        }

        for (Interest interest : interests) {
            userInterestRepository.save(new UserInterest(user, interest));
        }
    }
}
