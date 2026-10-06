package com.jin.studyreservation.domain.meeting.dto;

import com.jin.studyreservation.domain.meeting.entity.Category;
import com.jin.studyreservation.domain.meeting.entity.Meeting;
import com.jin.studyreservation.domain.meeting.entity.MeetingStatus;
import java.time.LocalDateTime;

public record MeetingSummaryResponse(
    Long id,
    String title,
    Category category,
    String location,
    LocalDateTime meetingAt,
    LocalDateTime recruitDeadline,
    int maxParticipants,
    int currentParticipants,
    MeetingStatus status
) {

  public static MeetingSummaryResponse of(Meeting meeting, LocalDateTime now) {
    return new MeetingSummaryResponse(
        meeting.getId(),
        meeting.getTitle(),
        meeting.getCategory(),
        meeting.getLocation(),
        meeting.getMeetingAt(),
        meeting.getRecruitDeadline(),
        meeting.getMaxParticipants(),
        meeting.getCurrentParticipants(),
        meeting.statusAt(now)
    );
  }
}
