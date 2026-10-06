package com.jin.studyreservation.domain.bookmark.repository;

import com.jin.studyreservation.domain.bookmark.entity.Bookmark;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

  boolean existsByMeeting_IdAndUser_Id(Long meetingId, Long userId);

  Optional<Bookmark> findByMeeting_IdAndUser_Id(Long meetingId, Long userId);

  @Query("select b.meeting.id from Bookmark b where b.user.id = :userId")
  List<Long> findMeetingIdsByUserId(@Param("userId") Long userId);
}
