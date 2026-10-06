package com.jin.studyreservation.domain.user.dto;

import com.jin.studyreservation.domain.user.entity.Provider;
import com.jin.studyreservation.domain.user.entity.User;

public record MeResponse(Long id, String nickname, String profileImageUrl, Provider provider) {

  public static MeResponse from(User user) {
    return new MeResponse(user.getId(), user.getNickname(), user.getProfileImageUrl(),
        user.getProvider());
  }
}
