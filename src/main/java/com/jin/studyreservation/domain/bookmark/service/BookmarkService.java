package com.jin.studyreservation.domain.bookmark.service;

import com.jin.studyreservation.domain.bookmark.entity.Bookmark;
import com.jin.studyreservation.domain.bookmark.repository.BookmarkRepository;
import com.jin.studyreservation.domain.meeting.entity.Meeting;
import com.jin.studyreservation.domain.meeting.service.MeetingService;
import com.jin.studyreservation.domain.user.entity.User;
import com.jin.studyreservation.domain.user.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** 북마크 추가, 해제는 멱등하게 동작한다 (버튼 연타, 재시도에 안전) */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

  private final BookmarkRepository bookmarkRepository;
  private final MeetingService meetingService;
  private final UserService userService;

  @Transactional
  public void add(Long meetingId, Long userId) {
    if (bookmarkRepository.existsByMeeting_IdAndUser_Id(meetingId, userId)) {
      return;
    }
    Meeting meeting = meetingService.getById(meetingId);
    User user = userService.getById(userId);
    bookmarkRepository.save(Bookmark.of(meeting, user));
  }

  @Transactional
  public void remove(Long meetingId, Long userId) {
    bookmarkRepository.findByMeeting_IdAndUser_Id(meetingId, userId)
        .ifPresent(bookmarkRepository::delete);
  }

  public boolean isBookmarked(Long meetingId, Long userId) {
    return bookmarkRepository.existsByMeeting_IdAndUser_Id(meetingId, userId);
  }

  public List<Long> getBookmarkedMeetingIds(Long userId) {
    return bookmarkRepository.findMeetingIdsByUserId(userId);
  }
}
