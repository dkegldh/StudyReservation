package com.jin.studyreservation.domain.meeting.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jin.studyreservation.domain.user.entity.User;
import com.jin.studyreservation.global.error.BusinessException;
import com.jin.studyreservation.global.error.ErrorCode;
import com.jin.studyreservation.support.Fixtures;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class MeetingTest {

  private static final Long HOST_ID = 1L;
  private static final Long USER_ID = 2L;

  private final LocalDateTime now = LocalDateTime.of(2026, 10, 6, 12, 0);
  private User host;

  @BeforeEach
  void setUp() {
    host = Fixtures.user("host");
    ReflectionTestUtils.setField(host, "id", HOST_ID);
  }

  @Test
  void 신청하면_참여자가_늘고_정원이_차면_CLOSED가_된다() {
    // given
    Meeting meeting = Fixtures.meeting(host, 2, now);

    // when
    meeting.participate(USER_ID, now);
    meeting.participate(3L, now);

    // then
    assertThat(meeting.getCurrentParticipants()).isEqualTo(2);
    assertThat(meeting.getStatus()).isEqualTo(MeetingStatus.CLOSED);
  }

  @Test
  void 정원이_가득_찬_모임에는_신청할_수_없다() {
    // given
    Meeting meeting = Fixtures.meeting(host, 1, now);
    meeting.participate(USER_ID, now);

    // when, then
    assertThatThrownBy(() -> meeting.participate(3L, now))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode").isEqualTo(ErrorCode.MEETING_FULL);
  }

  @Test
  void 모임장은_자기_모임에_신청할_수_없다() {
    Meeting meeting = Fixtures.meeting(host, 5, now);

    assertThatThrownBy(() -> meeting.participate(HOST_ID, now))
        .extracting("errorCode").isEqualTo(ErrorCode.HOST_CANNOT_PARTICIPATE);
  }

  @Test
  void 모집_마감_이후에는_신청할_수_없다() {
    Meeting meeting = Fixtures.meeting(host, 5, now);

    assertThatThrownBy(() -> meeting.participate(USER_ID, meeting.getRecruitDeadline()))
        .extracting("errorCode").isEqualTo(ErrorCode.RECRUITMENT_CLOSED);
  }

  @Test
  void 모임_시작_이후에는_신청도_취소도_할_수_없다() {
    Meeting meeting = Fixtures.meeting(host, 5, now);
    meeting.participate(USER_ID, now);

    assertThatThrownBy(() -> meeting.participate(3L, meeting.getMeetingAt()))
        .extracting("errorCode").isEqualTo(ErrorCode.MEETING_ALREADY_STARTED);
    assertThatThrownBy(() -> meeting.cancelParticipation(meeting.getMeetingAt()))
        .extracting("errorCode").isEqualTo(ErrorCode.MEETING_ALREADY_STARTED);
  }

  @Test
  void 취소된_모임에는_신청할_수_없다() {
    Meeting meeting = Fixtures.meeting(host, 5, now);
    meeting.cancel(now);

    assertThatThrownBy(() -> meeting.participate(USER_ID, now))
        .extracting("errorCode").isEqualTo(ErrorCode.MEETING_ALREADY_CANCELED);
  }

  @Test
  void 마감_전에_취소하면_CLOSED에서_RECRUITING으로_돌아간다() {
    // given
    Meeting meeting = Fixtures.meeting(host, 1, now);
    meeting.participate(USER_ID, now);

    // when
    meeting.cancelParticipation(now);

    // then
    assertThat(meeting.getCurrentParticipants()).isZero();
    assertThat(meeting.getStatus()).isEqualTo(MeetingStatus.RECRUITING);
  }

  @Test
  void 마감_이후에_취소하면_CLOSED를_유지한다() {
    // given
    Meeting meeting = Fixtures.meeting(host, 1, now);
    meeting.participate(USER_ID, now);

    // when
    meeting.cancelParticipation(meeting.getRecruitDeadline().plusMinutes(1));

    // then
    assertThat(meeting.getStatus()).isEqualTo(MeetingStatus.CLOSED);
  }

  @Test
  void 정원을_현재_참여자보다_적게_줄일_수_없다() {
    Meeting meeting = Fixtures.meeting(host, 3, now);
    meeting.participate(USER_ID, now);
    meeting.participate(3L, now);

    assertThatThrownBy(() -> meeting.update("t", "d", Category.STUDY, "l",
        meeting.getMeetingAt(), meeting.getRecruitDeadline(), 1, now))
        .extracting("errorCode").isEqualTo(ErrorCode.INVALID_MAX_PARTICIPANTS);
  }

  @Test
  void 모집_마감이_모임_시작보다_늦으면_만들_수_없다() {
    assertThatThrownBy(() -> Meeting.create(host, "t", "d", Category.STUDY, "l",
        now.plusDays(1), now.plusDays(2), 5, now))
        .extracting("errorCode").isEqualTo(ErrorCode.INVALID_MEETING_SCHEDULE);
  }

  @Test
  void 마감이_지난_모집중_모임은_화면에_CLOSED로_보인다() {
    Meeting meeting = Fixtures.meeting(host, 5, now);

    assertThat(meeting.statusAt(now)).isEqualTo(MeetingStatus.RECRUITING);
    assertThat(meeting.statusAt(meeting.getRecruitDeadline())).isEqualTo(MeetingStatus.CLOSED);
  }
}
