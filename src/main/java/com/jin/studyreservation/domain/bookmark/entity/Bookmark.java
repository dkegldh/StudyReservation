package com.jin.studyreservation.domain.bookmark.entity;

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
@Table(name = "bookmarks", uniqueConstraints = @UniqueConstraint(
    name = "uk_bookmarks_user_meeting", columnNames = {"user_id", "meeting_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bookmark extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "meeting_id", nullable = false)
  private Meeting meeting;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  private Bookmark(Meeting meeting, User user) {
    this.meeting = meeting;
    this.user = user;
  }

  public static Bookmark of(Meeting meeting, User user) {
    return new Bookmark(meeting, user);
  }
}
