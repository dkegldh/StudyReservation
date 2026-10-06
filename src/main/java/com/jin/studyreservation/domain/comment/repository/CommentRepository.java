package com.jin.studyreservation.domain.comment.repository;

import com.jin.studyreservation.domain.comment.entity.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  /** 작성자를 fetch join해서 댓글 수만큼 users 조회가 나가지 않게 한다 */
  @Query("select c from Comment c join fetch c.author where c.meeting.id = :meetingId order by c.id")
  List<Comment> findAllWithAuthorByMeetingId(@Param("meetingId") Long meetingId);
}
