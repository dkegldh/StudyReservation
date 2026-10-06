package com.jin.studyreservation.domain.meeting.dto;

import com.jin.studyreservation.domain.meeting.entity.Category;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/** 모임 생성, 수정 요청 (프론트 CreateMeetingRequest) */
public record MeetingRequest(
    @NotBlank @Size(max = 100) String title,
    @NotBlank @Size(max = 5000) String description,
    @NotNull Category category,
    @Min(2) @Max(100) int maxParticipants,
    @NotBlank @Size(max = 200) String location,
    @NotNull LocalDateTime meetingAt,
    @NotNull LocalDateTime recruitDeadline
) {

}
