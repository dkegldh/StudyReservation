package com.jin.studyreservation.domain.user.service;

import com.jin.studyreservation.domain.user.dto.MeResponse;
import com.jin.studyreservation.domain.user.entity.Provider;
import com.jin.studyreservation.domain.user.entity.User;
import com.jin.studyreservation.domain.user.repository.UserRepository;
import com.jin.studyreservation.global.error.BusinessException;
import com.jin.studyreservation.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;

  /** 다른 도메인 서비스가 사용자 엔티티를 참조할 때 사용 */
  public User getById(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
  }

  public MeResponse getMe(Long userId) {
    return MeResponse.from(getById(userId));
  }

  /** OAuth2 로그인 성공 시 호출: 처음이면 가입, 이미 있으면 프로필 동기화 */
  @Transactional
  public User loginSocialUser(Provider provider, String providerId, String nickname,
      String profileImageUrl) {
    return userRepository.findByProviderAndProviderId(provider, providerId)
        .map(user -> {
          user.syncProfile(nickname, profileImageUrl);
          return user;
        })
        .orElseGet(() -> userRepository.save(
            User.createSocialUser(provider, providerId, nickname, profileImageUrl)));
  }
}
