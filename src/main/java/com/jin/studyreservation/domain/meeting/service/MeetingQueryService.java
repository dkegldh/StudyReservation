package com.jin.studyreservation.domain.meeting.service;

import com.jin.studyreservation.domain.bookmark.service.BookmarkService;
import com.jin.studyreservation.domain.meeting.dto.MeetingDetailResponse;
import com.jin.studyreservation.domain.meeting.dto.MeetingSearchCondition;
import com.jin.studyreservation.domain.meeting.dto.MeetingSummaryResponse;
import com.jin.studyreservation.domain.meeting.dto.MyMeetingType;
import com.jin.studyreservation.domain.meeting.entity.Meeting;
import com.jin.studyreservation.domain.meeting.repository.MeetingRepository;
import com.jin.studyreservation.domain.participation.service.ParticipationService;
import com.jin.studyreservation.global.error.BusinessException;
import com.jin.studyreservation.global.error.ErrorCode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 모임 조회 전용. 참여, 북마크 여부를 함께 조립하므로 해당 도메인 서비스에 의존한다.
 * (MeetingService와 분리해 ParticipationService → MeetingService 방향과 순환 의존이 생기지 않게 한다)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingQueryService {

  private static final int MAX_PAGE_SIZE = 50;

  private final MeetingRepository meetingRepository;
  private final ParticipationService participationService;
  private final BookmarkService bookmarkService;
  private final Clock clock;

  public Page<MeetingSummaryResponse> search(MeetingSearchCondition condition, Pageable pageable) {
    LocalDateTime now = now();
    Pageable limited = pageable.getPageSize() > MAX_PAGE_SIZE
        ? PageRequest.of(pageable.getPageNumber(), MAX_PAGE_SIZE)
        : pageable;
    return meetingRepository.search(condition, now, limited)
        .map(meeting -> MeetingSummaryResponse.of(meeting, now));
  }

  /** userId가 null이면 비로그인: participating, bookmarked는 false */
  public MeetingDetailResponse getDetail(Long meetingId, Long userId) {
    Meeting meeting = meetingRepository.findWithHostById(meetingId)
        .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
    boolean participating =
        userId != null && participationService.isParticipating(meetingId, userId);
    boolean bookmarked = userId != null && bookmarkService.isBookmarked(meetingId, userId);
    return MeetingDetailResponse.of(meeting, now(), participating, bookmarked);
  }

  public List<MeetingSummaryResponse> getMyMeetings(Long userId, MyMeetingType type) {
    List<Meeting> meetings = switch (type) {
      case HOSTED -> meetingRepository.findAllByHost_IdOrderByMeetingAtDesc(userId);
      case JOINED -> findAllByIds(participationService.getJoinedMeetingIds(userId));
      case BOOKMARKED -> findAllByIds(bookmarkService.getBookmarkedMeetingIds(userId));
    };
    LocalDateTime now = now();
    return meetings.stream()
        .map(meeting -> MeetingSummaryResponse.of(meeting, now))
        .toList();
  }

  private List<Meeting> findAllByIds(List<Long> ids) {
    return ids.isEmpty() ? List.of() : meetingRepository.findAllByIdInOrderByMeetingAtDesc(ids);
  }

  private LocalDateTime now() {
    return LocalDateTime.now(clock);
  }
}
