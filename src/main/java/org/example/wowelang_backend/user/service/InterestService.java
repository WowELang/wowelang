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
        //join 테이블에 저장
        saveUserInterests(user, interestIds);
        user.setInterestsInitialized(true);
        userRepository.save(user);
    }
    //전체 관심사 목록 조회
    public List<InterestDto> getAllInterest() {
        return interestRepository.findAll().stream()
                .map(i -> new InterestDto(
                        i.getId(),
                        i.getName()))
                .toList();
    }

    //관심사 수정
    public void updateInterests(Long userId, List<Long> newInterestIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        if (!user.getInterestInitialized()) {
            throw new IllegalStateException(ErrorStatus.INTERESTS_NOT_IVITIALIZED.getMessage());
        }

        //1) 기존 저장된 관심사 ID 집합
        List<UserInterest> existingLinks = userInterestRepository.findAllByUserId(userId);
        Set<Long> existingIds = existingLinks.stream()
                .map(ui -> ui.getInterest().getId())
                .collect(Collectors.toSet());

        // 2) 요청으로 온 새 ID 집합
        Set<Long> newIds = new HashSet<>(newInterestIds);

        // 3) 삭제할 ID = existingIds \ newIds
        Set<Long> toRemove = new HashSet<>(existingIds);
        toRemove.removeAll(newIds);

        // 4) 추가할 ID = newIds \ existingIds
        Set<Long> toAdd = new HashSet<>(newIds);
        toAdd.removeAll(existingIds);

        // 5) 삭제
        if (!toRemove.isEmpty()) {
            userInterestRepository.deleteByUserIdAndInterestIdIn(userId, toRemove);
        }

        // 6) 추가
        if (!toAdd.isEmpty()) {
            List<Interest> interests = interestRepository.findAllById(toAdd);
            if (interests.size() != toAdd.size()) {
                throw new IllegalArgumentException(ErrorStatus.INVALID_INTEREST_ID.getMessage());
            }
            for (Interest i : interests) {
                userInterestRepository.save(new UserInterest(user, i));
            }
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
