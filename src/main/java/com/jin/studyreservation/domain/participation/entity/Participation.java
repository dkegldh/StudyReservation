package com.jin.studyreservation.domain.participation.entity;

import com.jin.studyreservation.domain.meeting.entity.Meeting;
import com.jin.studyreservation.domain.user.entity.User;
import com.jin.studyreservation.global.common.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "participations", uniqueConstraints = @UniqueConstraint(
    name = "uk_participations_meeting_user", columnNames = {"meeting_id", "user_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participation extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "meeting_id", nullable = false)
  private Meeting meeting;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  private Participation(Meeting meeting, User user) {
    this.meeting = meeting;
    this.user = user;
  }

  /** 정원, 상태 검증은 Meeting#participate가 담당하므로 반드시 그 뒤에 생성한다 */
  public static Participation of(Meeting meeting, User user) {
    return new Participation(meeting, user);
  }
}
