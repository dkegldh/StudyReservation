package com.jin.studyreservation.domain.meeting.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.jin.studyreservation.domain.bookmark.service.BookmarkService;
import com.jin.studyreservation.domain.meeting.dto.MeetingDetailResponse;
import com.jin.studyreservation.domain.meeting.dto.MeetingSearchCondition;
import com.jin.studyreservation.domain.meeting.dto.MeetingSummaryResponse;
import com.jin.studyreservation.domain.meeting.dto.MyMeetingType;
import com.jin.studyreservation.domain.meeting.entity.Category;
import com.jin.studyreservation.domain.meeting.entity.Meeting;
import com.jin.studyreservation.domain.meeting.entity.MeetingStatus;
import com.jin.studyreservation.domain.meeting.repository.MeetingRepository;
import com.jin.studyreservation.domain.participation.service.ParticipationFacade;
import com.jin.studyreservation.domain.user.entity.User;
import com.jin.studyreservation.domain.user.repository.UserRepository;
import com.jin.studyreservation.support.Fixtures;
import com.jin.studyreservation.support.IntegrationTestSupport;
import java.time.Clock;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

class MeetingQueryServiceTest extends IntegrationTestSupport {

  @Autowired
  private MeetingQueryService meetingQueryService;
  @Autowired
  private MeetingFacade meetingFacade;
  @Autowired
  private ParticipationFacade participationFacade;
  @Autowired
  private BookmarkService bookmarkService;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private MeetingRepository meetingRepository;
  @Autowired
  private Clock clock;

  private User host;
  private User user;
  private LocalDateTime now;

  @BeforeEach
  void setUp() {
    host = userRepository.save(Fixtures.user("host"));
    user = userRepository.save(Fixtures.user("user"));
    now = LocalDateTime.now(clock);
  }

  @Test
  void 카테고리_키워드_모집중_조건으로_검색하고_취소된_모임은_제외한다() {
    // given
    Meeting study = save("스프링 스터디", Category.STUDY, 5);
    save("러닝 모임", Category.SPORTS, 5);
    Meeting full = save("스프링 심화", Category.STUDY, 1);
    participationFacade.apply(full.getId(), user.getId());
    Meeting canceled = save("스프링 취소됨", Category.STUDY, 5);
    meetingFacade.cancel(canceled.getId(), host.getId());

    // when
    Page<MeetingSummaryResponse> all = meetingQueryService.search(
        new MeetingSearchCondition(Category.STUDY, null, "스프링"), PageRequest.of(0, 12));
    Page<MeetingSummaryResponse> recruiting = meetingQueryService.search(
        new MeetingSearchCondition(Category.STUDY, MeetingStatus.RECRUITING, "스프링"),
        PageRequest.of(0, 12));

    // then
    assertThat(all.getContent()).extracting(MeetingSummaryResponse::id)
        .containsExactlyInAnyOrder(study.getId(), full.getId());
    assertThat(recruiting.getContent()).extracting(MeetingSummaryResponse::id)
        .containsExactly(study.getId());
  }

  @Test
  void 상세_조회는_로그인_사용자_기준으로_참여_북마크_여부를_채운다() {
    // given
    Meeting meeting = save("스프링 스터디", Category.STUDY, 5);
    participationFacade.apply(meeting.getId(), user.getId());
    bookmarkService.add(meeting.getId(), user.getId());

    // when
    MeetingDetailResponse loggedIn = meetingQueryService.getDetail(meeting.getId(), user.getId());
    MeetingDetailResponse anonymous = meetingQueryService.getDetail(meeting.getId(), null);

    // then
    assertThat(loggedIn.participating()).isTrue();
    assertThat(loggedIn.bookmarked()).isTrue();
    assertThat(loggedIn.host().id()).isEqualTo(host.getId());
    assertThat(anonymous.participating()).isFalse();
    assertThat(anonymous.bookmarked()).isFalse();
  }

  @Test
  void 내_모임을_유형별로_조회한다() {
    // given
    Meeting hosted = save("내가 만든 모임", Category.HOBBY, 5);
    Meeting joined = meetingRepository.save(Fixtures.meeting(user, 5, now));
    participationFacade.apply(joined.getId(), host.getId());

    // when, then
    assertThat(meetingQueryService.getMyMeetings(host.getId(), MyMeetingType.HOSTED))
        .extracting(MeetingSummaryResponse::id).containsExactly(hosted.getId());
    assertThat(meetingQueryService.getMyMeetings(host.getId(), MyMeetingType.JOINED))
        .extracting(MeetingSummaryResponse::id).containsExactly(joined.getId());
    assertThat(meetingQueryService.getMyMeetings(host.getId(), MyMeetingType.BOOKMARKED))
        .isEmpty();
  }

  private Meeting save(String title, Category category, int max) {
    return meetingRepository.save(Meeting.create(host, title, "설명", category, "판교역",
        now.plusDays(4), now.plusDays(3), max, now));
  }
}
