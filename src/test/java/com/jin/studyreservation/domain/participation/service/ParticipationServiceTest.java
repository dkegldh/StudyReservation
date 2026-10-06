package com.jin.studyreservation.domain.participation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jin.studyreservation.domain.meeting.entity.Meeting;
import com.jin.studyreservation.domain.meeting.entity.MeetingStatus;
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

class ParticipationServiceTest extends IntegrationTestSupport {

  @Autowired
  private ParticipationFacade participationFacade;
  @Autowired
  private ParticipationService participationService;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private MeetingRepository meetingRepository;
  @Autowired
  private Clock clock;

  private User host;
  private User user;

  @BeforeEach
  void setUp() {
    host = userRepository.save(Fixtures.user("host"));
    user = userRepository.save(Fixtures.user("user"));
  }

  @Test
  void 신청하면_참여_상태가_되고_취소하면_해제된다() {
    // given
    Meeting meeting = saveMeeting(1);

    // when
    participationFacade.apply(meeting.getId(), user.getId());

    // then
    assertThat(participationService.isParticipating(meeting.getId(), user.getId())).isTrue();
    assertThat(reload(meeting).getStatus()).isEqualTo(MeetingStatus.CLOSED);

    // when
    participationFacade.cancel(meeting.getId(), user.getId());

    // then
    assertThat(participationService.isParticipating(meeting.getId(), user.getId())).isFalse();
    assertThat(reload(meeting).getCurrentParticipants()).isZero();
    assertThat(reload(meeting).getStatus()).isEqualTo(MeetingStatus.RECRUITING);
  }

  @Test
  void 이미_신청한_모임에_다시_신청할_수_없다() {
    Meeting meeting = saveMeeting(5);
    participationFacade.apply(meeting.getId(), user.getId());

    assertThatThrownBy(() -> participationFacade.apply(meeting.getId(), user.getId()))
        .extracting("errorCode").isEqualTo(ErrorCode.ALREADY_PARTICIPATING);
    assertThat(reload(meeting).getCurrentParticipants()).isEqualTo(1);
  }

  @Test
  void 신청하지_않은_모임은_취소할_수_없다() {
    Meeting meeting = saveMeeting(5);

    assertThatThrownBy(() -> participationFacade.cancel(meeting.getId(), user.getId()))
        .extracting("errorCode").isEqualTo(ErrorCode.NOT_PARTICIPATING);
  }

  @Test
  void 없는_모임에는_신청할_수_없다() {
    assertThatThrownBy(() -> participationFacade.apply(999L, user.getId()))
        .extracting("errorCode").isEqualTo(ErrorCode.MEETING_NOT_FOUND);
  }

  private Meeting saveMeeting(int max) {
    return meetingRepository.save(Fixtures.meeting(host, max, LocalDateTime.now(clock)));
  }

  private Meeting reload(Meeting meeting) {
    return meetingRepository.findById(meeting.getId()).orElseThrow();
  }
}
