package com.jin.studyreservation.domain.meeting.entity;

import com.jin.studyreservation.domain.user.entity.User;
import com.jin.studyreservation.global.common.BaseTimeEntity;
import com.jin.studyreservation.global.error.BusinessException;
import com.jin.studyreservation.global.error.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * current_participants를 바꾸는 메서드(participate, cancelParticipation, update, cancel)는
 * 반드시 lock:meeting:{id} 분산 락 안의 트랜잭션에서 호출해야 한다.
 */
@Getter
@Entity
@Table(name = "meetings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Meeting extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "host_id", nullable = false)
  private User host;

  @Column(nullable = false, length = 100)
  private String title;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Category category;

  @Column(nullable = false, length = 200)
  private String location;

  @Column(nullable = false)
  private LocalDateTime meetingAt;

  @Column(nullable = false)
  private LocalDateTime recruitDeadline;

  @Column(nullable = false)
  private int maxParticipants;

  @Column(nullable = false)
  private int currentParticipants;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private MeetingStatus status;

  private Meeting(User host, String title, String description, Category category, String location,
      LocalDateTime meetingAt, LocalDateTime recruitDeadline, int maxParticipants) {
    this.host = host;
    this.title = title;
    this.description = description;
    this.category = category;
    this.location = location;
    this.meetingAt = meetingAt;
    this.recruitDeadline = recruitDeadline;
    this.maxParticipants = maxParticipants;
    this.currentParticipants = 0;
    this.status = MeetingStatus.RECRUITING;
  }

  public static Meeting create(User host, String title, String description, Category category,
      String location, LocalDateTime meetingAt, LocalDateTime recruitDeadline, int maxParticipants,
      LocalDateTime now) {
    validateSchedule(meetingAt, recruitDeadline, now);
    if (maxParticipants < 1) {
      throw new BusinessException(ErrorCode.INVALID_INPUT);
    }
    return new Meeting(host, title, description, category, location, meetingAt, recruitDeadline,
        maxParticipants);
  }

  public void update(String title, String description, Category category, String location,
      LocalDateTime meetingAt, LocalDateTime recruitDeadline, int maxParticipants,
      LocalDateTime now) {
    validateModifiable(now);
    validateSchedule(meetingAt, recruitDeadline, now);
    if (maxParticipants < currentParticipants) {
      throw new BusinessException(ErrorCode.INVALID_MAX_PARTICIPANTS);
    }
    this.title = title;
    this.description = description;
    this.category = category;
    this.location = location;
    this.meetingAt = meetingAt;
    this.recruitDeadline = recruitDeadline;
    this.maxParticipants = maxParticipants;
    this.status = isFull() ? MeetingStatus.CLOSED : MeetingStatus.RECRUITING;
  }

  /** 모임장이 모임 자체를 취소 */
  public void cancel(LocalDateTime now) {
    validateModifiable(now);
    this.status = MeetingStatus.CANCELED;
  }

  public void participate(Long userId, LocalDateTime now) {
    validateModifiable(now);
    if (isHostedBy(userId)) {
      throw new BusinessException(ErrorCode.HOST_CANNOT_PARTICIPATE);
    }
    if (!now.isBefore(recruitDeadline)) {
      throw new BusinessException(ErrorCode.RECRUITMENT_CLOSED);
    }
    if (isFull()) {
      throw new BusinessException(ErrorCode.MEETING_FULL);
    }
    if (status != MeetingStatus.RECRUITING) {
      throw new BusinessException(ErrorCode.RECRUITMENT_CLOSED);
    }
    currentParticipants++;
    if (isFull()) {
      status = MeetingStatus.CLOSED;
    }
  }

  public void cancelParticipation(LocalDateTime now) {
    validateModifiable(now);
    if (currentParticipants <= 0) {
      throw new BusinessException(ErrorCode.NOT_PARTICIPATING);
    }
    currentParticipants--;
    if (status == MeetingStatus.CLOSED && now.isBefore(recruitDeadline)) {
      status = MeetingStatus.RECRUITING;
    }
  }

  public boolean isHostedBy(Long userId) {
    return host.getId().equals(userId);
  }

  public void validateHost(Long userId) {
    if (!isHostedBy(userId)) {
      throw new BusinessException(ErrorCode.NOT_MEETING_HOST);
    }
  }

  public boolean isFull() {
    return currentParticipants >= maxParticipants;
  }

  /**
   * 화면에 보여 줄 상태. 마감 시각이 지나도 DB 상태는 RECRUITING으로 남아 있으므로(스케줄러 없음),
   * 응답에서는 마감이 지난 모집 중 모임을 CLOSED로 보여 준다.
   */
  public MeetingStatus statusAt(LocalDateTime now) {
    if (status == MeetingStatus.RECRUITING && !now.isBefore(recruitDeadline)) {
      return MeetingStatus.CLOSED;
    }
    return status;
  }

  private void validateModifiable(LocalDateTime now) {
    if (status == MeetingStatus.CANCELED) {
      throw new BusinessException(ErrorCode.MEETING_ALREADY_CANCELED);
    }
    if (!now.isBefore(meetingAt)) {
      throw new BusinessException(ErrorCode.MEETING_ALREADY_STARTED);
    }
  }

  private static void validateSchedule(LocalDateTime meetingAt, LocalDateTime recruitDeadline,
      LocalDateTime now) {
    if (!recruitDeadline.isAfter(now) || recruitDeadline.isAfter(meetingAt)) {
      throw new BusinessException(ErrorCode.INVALID_MEETING_SCHEDULE);
    }
  }
}
