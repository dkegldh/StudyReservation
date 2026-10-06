package com.jin.studyreservation.domain.comment.service;

import com.jin.studyreservation.domain.comment.dto.CommentCreateRequest;
import com.jin.studyreservation.domain.comment.dto.CommentResponse;
import com.jin.studyreservation.domain.comment.entity.Comment;
import com.jin.studyreservation.domain.comment.repository.CommentRepository;
import com.jin.studyreservation.domain.meeting.entity.Meeting;
import com.jin.studyreservation.domain.meeting.service.MeetingService;
import com.jin.studyreservation.domain.user.entity.User;
import com.jin.studyreservation.domain.user.service.UserService;
import com.jin.studyreservation.global.error.BusinessException;
import com.jin.studyreservation.global.error.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

  private final CommentRepository commentRepository;
  private final MeetingService meetingService;
  private final UserService userService;

  public List<CommentResponse> getComments(Long meetingId) {
    meetingService.getById(meetingId);
    return commentRepository.findAllWithAuthorByMeetingId(meetingId).stream()
        .map(CommentResponse::from)
        .toList();
  }

  @Transactional
  public CommentResponse create(Long meetingId, Long authorId, CommentCreateRequest request) {
    Meeting meeting = meetingService.getById(meetingId);
    User author = userService.getById(authorId);
    Comment comment = request.parentId() == null
        ? Comment.create(meeting, author, request.content())
        : Comment.createReply(meeting, author, getById(request.parentId()), request.content());
    return CommentResponse.from(commentRepository.save(comment));
  }

  @Transactional
  public void delete(Long commentId, Long userId) {
    getById(commentId).delete(userId);
  }

  private Comment getById(Long commentId) {
    return commentRepository.findById(commentId)
        .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
  }
}
