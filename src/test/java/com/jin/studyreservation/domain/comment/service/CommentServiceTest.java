package com.jin.studyreservation.domain.comment.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jin.studyreservation.domain.comment.dto.CommentCreateRequest;
import com.jin.studyreservation.domain.comment.dto.CommentResponse;
import com.jin.studyreservation.domain.meeting.entity.Meeting;
import com.jin.studyreservation.domain.meeting.repository.MeetingRepository;
import com.jin.studyreservation.domain.user.entity.User;
import com.jin.studyreservation.domain.user.repository.UserRepository;
import com.jin.studyreservation.global.error.ErrorCode;
import com.jin.studyreservation.support.Fixtures;
import com.jin.studyreservation.support.IntegrationTestSupport;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CommentServiceTest extends IntegrationTestSupport {

  @Autowired
  private CommentService commentService;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private MeetingRepository meetingRepository;
  @Autowired
  private Clock clock;

  private User author;
  private User other;
  private Meeting meeting;

  @BeforeEach
  void setUp() {
    author = userRepository.save(Fixtures.user("author"));
    other = userRepository.save(Fixtures.user("other"));
    meeting = meetingRepository.save(Fixtures.meeting(author, 5, LocalDateTime.now(clock)));
  }

  @Test
  void 댓글과_답글을_작성하고_작성_순서대로_조회한다() {
    // given
    CommentResponse parent = commentService.create(meeting.getId(), author.getId(),
        new CommentCreateRequest("참여해도 될까요?", null));

    // when
    CommentResponse reply = commentService.create(meeting.getId(), other.getId(),
        new CommentCreateRequest("네, 환영해요!", parent.id()));
    List<CommentResponse> comments = commentService.getComments(meeting.getId());

    // then
    assertThat(reply.parentId()).isEqualTo(parent.id());
    assertThat(comments).extracting(CommentResponse::id).containsExactly(parent.id(), reply.id());
    assertThat(comments.get(1).author().id()).isEqualTo(other.getId());
    assertThat(comments.get(0).createdAt()).isNotNull();
  }

  @Test
  void 답글에는_다시_답글을_달_수_없다() {
    CommentResponse parent = commentService.create(meeting.getId(), author.getId(),
        new CommentCreateRequest("부모", null));
    CommentResponse reply = commentService.create(meeting.getId(), other.getId(),
        new CommentCreateRequest("답글", parent.id()));

    assertThatThrownBy(() -> commentService.create(meeting.getId(), author.getId(),
        new CommentCreateRequest("답글의 답글", reply.id())))
        .extracting("errorCode").isEqualTo(ErrorCode.INVALID_PARENT_COMMENT);
  }

  @Test
  void 다른_모임의_댓글에는_답글을_달_수_없다() {
    Meeting another = meetingRepository.save(
        Fixtures.meeting(author, 5, LocalDateTime.now(clock)));
    CommentResponse parent = commentService.create(another.getId(), author.getId(),
        new CommentCreateRequest("다른 모임 댓글", null));

    assertThatThrownBy(() -> commentService.create(meeting.getId(), other.getId(),
        new CommentCreateRequest("답글", parent.id())))
        .extracting("errorCode").isEqualTo(ErrorCode.INVALID_PARENT_COMMENT);
  }

  @Test
  void 없는_댓글에는_답글을_달_수_없다() {
    assertThatThrownBy(() -> commentService.create(meeting.getId(), author.getId(),
        new CommentCreateRequest("답글", 999L)))
        .extracting("errorCode").isEqualTo(ErrorCode.COMMENT_NOT_FOUND);
  }

  @Test
  void 삭제한_댓글은_내용이_가려지고_답글_구조는_유지된다() {
    // given
    CommentResponse parent = commentService.create(meeting.getId(), author.getId(),
        new CommentCreateRequest("지울 댓글", null));
    commentService.create(meeting.getId(), other.getId(),
        new CommentCreateRequest("답글", parent.id()));

    // when
    commentService.delete(parent.id(), author.getId());

    // then
    List<CommentResponse> comments = commentService.getComments(meeting.getId());
    assertThat(comments).hasSize(2);
    assertThat(comments.get(0).deleted()).isTrue();
    assertThat(comments.get(0).content()).isEqualTo("삭제된 댓글이에요.");
  }

  @Test
  void 작성자가_아니면_댓글을_삭제할_수_없고_이미_삭제한_댓글은_다시_삭제할_수_없다() {
    CommentResponse comment = commentService.create(meeting.getId(), author.getId(),
        new CommentCreateRequest("댓글", null));

    assertThatThrownBy(() -> commentService.delete(comment.id(), other.getId()))
        .extracting("errorCode").isEqualTo(ErrorCode.NOT_COMMENT_AUTHOR);

    commentService.delete(comment.id(), author.getId());
    assertThatThrownBy(() -> commentService.delete(comment.id(), author.getId()))
        .extracting("errorCode").isEqualTo(ErrorCode.COMMENT_ALREADY_DELETED);
  }

  @Test
  void 없는_모임의_댓글은_조회할_수_없다() {
    assertThatThrownBy(() -> commentService.getComments(999L))
        .extracting("errorCode").isEqualTo(ErrorCode.MEETING_NOT_FOUND);
  }
}
