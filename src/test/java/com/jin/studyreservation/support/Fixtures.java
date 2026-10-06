package com.jin.studyreservation.support;

import com.jin.studyreservation.domain.meeting.entity.Category;
import com.jin.studyreservation.domain.meeting.entity.Meeting;
import com.jin.studyreservation.domain.user.entity.Provider;
import com.jin.studyreservation.domain.user.entity.User;
import java.time.LocalDateTime;

public final class Fixtures {

  private Fixtures() {
  }

  public static User user(String providerId) {
    return User.createSocialUser(Provider.KAKAO, providerId, "user-" + providerId, null);
  }

  /** 모집 마감 3일 뒤, 모임 시작 4일 뒤 */
  public static Meeting meeting(User host, int maxParticipants, LocalDateTime now) {
    return Meeting.create(host, "스프링 스터디", "같이 공부해요", Category.STUDY, "판교역",
        now.plusDays(4), now.plusDays(3), maxParticipants, now);
  }
}
