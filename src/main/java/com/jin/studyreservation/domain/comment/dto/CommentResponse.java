package com.jin.studyreservation.domain.comment.dto;

import com.jin.studyreservation.domain.comment.entity.Comment;
import com.jin.studyreservation.domain.user.dto.UserSummaryResponse;
import java.time.LocalDateTime;

public record CommentResponse(
    Long id,
    String content,
    UserSummaryResponse author,
    Long parentId,
    boolean deleted,
    LocalDateTime createdAt
) {

  private static final String DELETED_CONTENT = "삭제된 댓글이에요.";

  /** comment.author가 로딩된 상태여야 한다 */
  public static CommentResponse from(Comment comment) {
    return new CommentResponse(
        comment.getId(),
        comment.isDeleted() ? DELETED_CONTENT : comment.getContent(),
        UserSummaryResponse.from(comment.getAuthor()),
        comment.getParentId(),
        comment.isDeleted(),
        comment.getCreatedAt()
    );
  }
}
