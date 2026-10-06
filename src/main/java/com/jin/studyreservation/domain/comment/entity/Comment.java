package com.jin.studyreservation.domain.comment.entity;

import com.jin.studyreservation.domain.meeting.entity.Meeting;
import com.jin.studyreservation.domain.user.entity.User;
import com.jin.studyreservation.global.common.BaseTimeEntity;
import com.jin.studyreservation.global.error.BusinessException;
import com.jin.studyreservation.global.error.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 답글은 1단계까지만 허용한다. 삭제는 답글 구조를 유지하기 위해 소프트 삭제 */
@Getter
@Entity
@Table(name = "comments")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "meeting_id", nullable = false)
  private Meeting meeting;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "author_id", nullable = false)
  private User author;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_id")
  private Comment parent;

  @Column(nullable = false, length = 1000)
  private String content;

  @Column(nullable = false)
  private boolean deleted;

  private Comment(Meeting meeting, User author, Comment parent, String content) {
    this.meeting = meeting;
    this.author = author;
    this.parent = parent;
    this.content = content;
    this.deleted = false;
  }

  public static Comment create(Meeting meeting, User author, String content) {
    return new Comment(meeting, author, null, content);
  }

  public static Comment createReply(Meeting meeting, User author, Comment parent, String content) {
    boolean sameMeeting = parent.getMeeting().getId().equals(meeting.getId());
    if (!sameMeeting || parent.getParent() != null || parent.isDeleted()) {
      throw new BusinessException(ErrorCode.INVALID_PARENT_COMMENT);
    }
    return new Comment(meeting, author, parent, content);
  }

  public void delete(Long userId) {
    if (!author.getId().equals(userId)) {
      throw new BusinessException(ErrorCode.NOT_COMMENT_AUTHOR);
    }
    if (deleted) {
      throw new BusinessException(ErrorCode.COMMENT_ALREADY_DELETED);
    }
    this.deleted = true;
  }

  public Long getParentId() {
    return parent == null ? null : parent.getId();
  }
}
