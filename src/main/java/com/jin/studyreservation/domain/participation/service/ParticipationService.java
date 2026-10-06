package com.jin.studyreservation.domain.participation.service;

import com.jin.studyreservation.domain.meeting.entity.Meeting;
import com.jin.studyreservation.domain.meeting.service.MeetingService;
import com.jin.studyreservation.domain.participation.entity.Participation;
import com.jin.studyreservation.domain.participation.repository.ParticipationRepository;
import com.jin.studyreservation.domain.user.entity.User;
import com.jin.studyreservation.domain.user.service.UserService;
import com.jin.studyreservation.global.error.BusinessException;
import com.jin.studyreservation.global.error.ErrorCode;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * apply, cancel은 락 없이 호출하면 정원을 초과할 수 있다. 반드시 {@link ParticipationFacade}를 거친다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationService {

  private final ParticipationRepository participationRepository;
  private final MeetingService meetingService;
  private final UserService userService;
  private final Clock clock;

  @Transactional
  public void apply(Long meetingId, Long userId) {
    Meeting meeting = meetingService.getById(meetingId);
    if (participationRepository.existsByMeeting_IdAndUser_Id(meetingId, userId)) {
      throw new BusinessException(ErrorCode.ALREADY_PARTICIPATING);
    }
    User user = userService.getById(userId);
    meeting.participate(userId, LocalDateTime.now(clock));
    participationRepository.save(Participation.of(meeting, user));
  }

  @Transactional
  public void cancel(Long meetingId, Long userId) {
    Meeting meeting = meetingService.getById(meetingId);
    Participation participation = participationRepository
        .findByMeeting_IdAndUser_Id(meetingId, userId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_PARTICIPATING));
    meeting.cancelParticipation(LocalDateTime.now(clock));
    participationRepository.delete(participation);
  }

  public boolean isParticipating(Long meetingId, Long userId) {
    return participationRepository.existsByMeeting_IdAndUser_Id(meetingId, userId);
  }

  public List<Long> getJoinedMeetingIds(Long userId) {
    return participationRepository.findMeetingIdsByUserId(userId);
  }
}
