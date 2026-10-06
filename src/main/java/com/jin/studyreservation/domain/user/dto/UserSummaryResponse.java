package com.jin.studyreservation.domain.user.dto;

import com.jin.studyreservation.domain.user.entity.User;

public record UserSummaryResponse(Long id, String nickname, String profileImageUrl) {

  public static UserSummaryResponse from(User user) {
    return new UserSummaryResponse(user.getId(), user.getNickname(), user.getProfileImageUrl());
  }
}
