package com.jin.studyreservation.domain.meeting.service;

import com.jin.studyreservation.domain.meeting.dto.MeetingIdResponse;
import com.jin.studyreservation.domain.meeting.dto.MeetingRequest;
import com.jin.studyreservation.domain.meeting.entity.Meeting;
import com.jin.studyreservation.domain.meeting.repository.MeetingRepository;
import com.jin.studyreservation.domain.user.entity.User;
import com.jin.studyreservation.domain.user.service.UserService;
import com.jin.studyreservation.global.error.BusinessException;
import com.jin.studyreservation.global.error.ErrorCode;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 모임 쓰기와 엔티티 조회.
 * update, cancel, delete는 current_participants를 덮어쓸 수 있으므로 {@link MeetingFacade}를 통해 락 안에서 호출한다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MeetingService {

  private final MeetingRepository meetingRepository;
  private final UserService userService;
  private final Clock clock;

  /** 다른 도메인 서비스가 모임 엔티티를 참조할 때 사용 */
  public Meeting getById(Long meetingId) {
    return meetingRepository.findById(meetingId)
        .orElseThrow(() -> new BusinessException(ErrorCode.MEETING_NOT_FOUND));
  }

  @Transactional
  public MeetingIdResponse create(Long hostId, MeetingRequest request) {
    User host = userService.getById(hostId);
    Meeting meeting = Meeting.create(host, request.title(), request.description(),
        request.category(), request.location(), request.meetingAt(), request.recruitDeadline(),
        request.maxParticipants(), now());
    return new MeetingIdResponse(meetingRepository.save(meeting).getId());
  }

  @Transactional
  public void update(Long meetingId, Long userId, MeetingRequest request) {
    Meeting meeting = getById(meetingId);
    meeting.validateHost(userId);
    meeting.update(request.title(), request.description(), request.category(),
        request.location(), request.meetingAt(), request.recruitDeadline(),
        request.maxParticipants(), now());
  }

  @Transactional
  public void cancel(Long meetingId, Long userId) {
    Meeting meeting = getById(meetingId);
    meeting.validateHost(userId);
    meeting.cancel(now());
  }

  /** 참여자가 없을 때만 삭제한다. 댓글, 북마크는 FK ON DELETE CASCADE로 함께 지워진다 */
  @Transactional
  public void delete(Long meetingId, Long userId) {
    Meeting meeting = getById(meetingId);
    meeting.validateHost(userId);
    if (meeting.getCurrentParticipants() > 0) {
      throw new BusinessException(ErrorCode.MEETING_HAS_PARTICIPANTS);
    }
    meetingRepository.delete(meeting);
  }

  private LocalDateTime now() {
    return LocalDateTime.now(clock);
  }
}
