package com.jin.studyreservation.domain.meeting.dto;

import com.jin.studyreservation.domain.meeting.entity.Category;
import com.jin.studyreservation.domain.meeting.entity.Meeting;
import com.jin.studyreservation.domain.meeting.entity.MeetingStatus;
import com.jin.studyreservation.domain.user.dto.UserSummaryResponse;
import java.time.LocalDateTime;

public record MeetingDetailResponse(
    Long id,
    String title,
    Category category,
    String location,
    LocalDateTime meetingAt,
    LocalDateTime recruitDeadline,
    int maxParticipants,
    int currentParticipants,
    MeetingStatus status,
    String description,
    UserSummaryResponse host,
    boolean participating,
    boolean bookmarked
) {

  /** meeting.host가 fetch join으로 로딩된 상태여야 한다 */
  public static MeetingDetailResponse of(Meeting meeting, LocalDateTime now, boolean participating,
      boolean bookmarked) {
    return new MeetingDetailResponse(
        meeting.getId(),
        meeting.getTitle(),
        meeting.getCategory(),
        meeting.getLocation(),
        meeting.getMeetingAt(),
        meeting.getRecruitDeadline(),
        meeting.getMaxParticipants(),
        meeting.getCurrentParticipants(),
        meeting.statusAt(now),
        meeting.getDescription(),
        UserSummaryResponse.from(meeting.getHost()),
        participating,
        bookmarked
    );
  }
}
