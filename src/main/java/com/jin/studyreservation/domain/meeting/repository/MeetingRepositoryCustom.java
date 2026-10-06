package com.jin.studyreservation.domain.meeting.repository;

import com.jin.studyreservation.domain.meeting.dto.MeetingSearchCondition;
import com.jin.studyreservation.domain.meeting.entity.Meeting;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MeetingRepositoryCustom {

  Page<Meeting> search(MeetingSearchCondition condition, LocalDateTime now, Pageable pageable);
}
