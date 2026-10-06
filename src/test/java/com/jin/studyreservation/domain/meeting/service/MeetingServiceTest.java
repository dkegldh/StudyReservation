package com.jin.studyreservation.domain.meeting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jin.studyreservation.domain.meeting.dto.MeetingRequest;
import com.jin.studyreservation.domain.meeting.entity.Category;
import com.jin.studyreservation.domain.meeting.entity.Meeting;
import com.jin.studyreservation.domain.meeting.entity.MeetingStatus;
import com.jin.studyreservation.domain.meeting.repository.MeetingRepository;
import com.jin.studyreservation.domain.participation.service.ParticipationFacade;
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

class MeetingServiceTest extends IntegrationTestSupport {

  @Autowired
  private MeetingService meetingService;
  @Autowired
  private MeetingFacade meetingFacade;
  @Autowired
  private ParticipationFacade participationFacade;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private MeetingRepository meetingRepository;
  @Autowired
  private Clock clock;

  private User host;
  private User other;
  private LocalDateTime now;

  @BeforeEach
  void setUp() {
    host = userRepository.save(Fixtures.user("host"));
    other = userRepository.save(Fixtures.user("other"));
    now = LocalDateTime.now(clock);
  }

  @Test
  void 모임을_만들면_모집중_상태로_저장된다() {
    // when
    Long id = meetingService.create(host.getId(), request("새 모임", 5)).id();

    // then
    Meeting saved = meetingRepository.findWithHostById(id).orElseThrow();
    assertThat(saved.getTitle()).isEqualTo("새 모임");
    assertThat(saved.getHost().getId()).isEqualTo(host.getId());
    assertThat(saved.getStatus()).isEqualTo(MeetingStatus.RECRUITING);
    assertThat(saved.getCurrentParticipants()).isZero();
  }

  @Test
  void 없는_사용자는_모임을_만들_수_없다() {
    assertThatThrownBy(() -> meetingService.create(999L, request("새 모임", 5)))
        .extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_FOUND);
  }

  @Test
  void 모임장은_모임을_수정할_수_있다() {
    // given
    Long id = meetingService.create(host.getId(), request("원래 제목", 5)).id();

    // when
    meetingFacade.update(id, host.getId(), request("바뀐 제목", 8));

    // then
    Meeting updated = meetingRepository.findById(id).orElseThrow();
    assertThat(updated.getTitle()).isEqualTo("바뀐 제목");
    assertThat(updated.getMaxParticipants()).isEqualTo(8);
  }

  @Test
  void 정원을_늘리면_마감됐던_모임이_다시_모집중이_된다() {
    // given
    Long id = meetingService.create(host.getId(), request("모임", 2)).id();
    participationFacade.apply(id, other.getId());
    participationFacade.apply(id, userRepository.save(Fixtures.user("third")).getId());

    // when
    meetingFacade.update(id, host.getId(), request("모임", 3));

    // then
    assertThat(meetingRepository.findById(id).orElseThrow().getStatus())
        .isEqualTo(MeetingStatus.RECRUITING);
  }

  @Test
  void 모임장이_아니면_수정_취소_삭제할_수_없다() {
    Long id = meetingService.create(host.getId(), request("모임", 5)).id();

    assertThatThrownBy(() -> meetingFacade.update(id, other.getId(), request("x", 5)))
        .extracting("errorCode").isEqualTo(ErrorCode.NOT_MEETING_HOST);
    assertThatThrownBy(() -> meetingFacade.cancel(id, other.getId()))
        .extracting("errorCode").isEqualTo(ErrorCode.NOT_MEETING_HOST);
    assertThatThrownBy(() -> meetingFacade.delete(id, other.getId()))
        .extracting("errorCode").isEqualTo(ErrorCode.NOT_MEETING_HOST);
  }

  @Test
  void 모임장은_모임을_취소할_수_있다() {
    Long id = meetingService.create(host.getId(), request("모임", 5)).id();

    meetingFacade.cancel(id, host.getId());

    assertThat(meetingRepository.findById(id).orElseThrow().getStatus())
        .isEqualTo(MeetingStatus.CANCELED);
  }

  @Test
  void 참여자가_없는_모임은_삭제된다() {
    Long id = meetingService.create(host.getId(), request("모임", 5)).id();

    meetingFacade.delete(id, host.getId());

    assertThat(meetingRepository.findById(id)).isEmpty();
  }

  @Test
  void 참여자가_있는_모임은_삭제할_수_없다() {
    Long id = meetingService.create(host.getId(), request("모임", 5)).id();
    participationFacade.apply(id, other.getId());

    assertThatThrownBy(() -> meetingFacade.delete(id, host.getId()))
        .extracting("errorCode").isEqualTo(ErrorCode.MEETING_HAS_PARTICIPANTS);
    assertThat(meetingRepository.findById(id)).isPresent();
  }

  @Test
  void 없는_모임은_찾을_수_없다() {
    assertThatThrownBy(() -> meetingService.getById(999L))
        .extracting("errorCode").isEqualTo(ErrorCode.MEETING_NOT_FOUND);
  }

  private MeetingRequest request(String title, int max) {
    return new MeetingRequest(title, "설명", Category.STUDY, max, "판교역",
        now.plusDays(4), now.plusDays(3));
  }
}
