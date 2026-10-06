package com.jin.studyreservation.domain.bookmark.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jin.studyreservation.domain.meeting.entity.Meeting;
import com.jin.studyreservation.domain.meeting.repository.MeetingRepository;
import com.jin.studyreservation.domain.user.entity.User;
import com.jin.studyreservation.domain.user.repository.UserRepository;
import com.jin.studyreservation.global.error.ErrorCode;
import com.jin.studyreservation.support.Fixtures;
import com.jin.studyreservation.support.IntegrationTestSupport;
import java.time.Clock;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class BookmarkServiceTest extends IntegrationTestSupport {

  @Autowired
  private BookmarkService bookmarkService;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private MeetingRepository meetingRepository;
  @Autowired
  private Clock clock;

  private User user;
  private Meeting meeting;

  @BeforeEach
  void setUp() {
    User host = userRepository.save(Fixtures.user("host"));
    user = userRepository.save(Fixtures.user("user"));
    meeting = meetingRepository.save(Fixtures.meeting(host, 5, LocalDateTime.now(clock)));
  }

  @Test
  void 북마크를_여러_번_추가해도_하나만_저장된다() {
    // when
    bookmarkService.add(meeting.getId(), user.getId());
    bookmarkService.add(meeting.getId(), user.getId());

    // then
    assertThat(bookmarkService.isBookmarked(meeting.getId(), user.getId())).isTrue();
    assertThat(bookmarkService.getBookmarkedMeetingIds(user.getId()))
        .containsExactly(meeting.getId());
  }

  @Test
  void 북마크를_해제하면_목록에서_빠지고_없는_북마크_해제는_무시된다() {
    // given
    bookmarkService.add(meeting.getId(), user.getId());

    // when
    bookmarkService.remove(meeting.getId(), user.getId());
    bookmarkService.remove(meeting.getId(), user.getId());

    // then
    assertThat(bookmarkService.isBookmarked(meeting.getId(), user.getId())).isFalse();
    assertThat(bookmarkService.getBookmarkedMeetingIds(user.getId())).isEmpty();
  }

  @Test
  void 없는_모임은_북마크할_수_없다() {
    assertThatThrownBy(() -> bookmarkService.add(999L, user.getId()))
        .extracting("errorCode").isEqualTo(ErrorCode.MEETING_NOT_FOUND);
  }
}
