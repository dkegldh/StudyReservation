package com.jin.studyreservation.domain.meeting.repository;

import com.jin.studyreservation.domain.meeting.entity.Meeting;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MeetingRepository extends JpaRepository<Meeting, Long>, MeetingRepositoryCustom {

  @Query("select m from Meeting m join fetch m.host where m.id = :id")
  Optional<Meeting> findWithHostById(@Param("id") Long id);

  List<Meeting> findAllByHost_IdOrderByMeetingAtDesc(Long hostId);

  List<Meeting> findAllByIdInOrderByMeetingAtDesc(Collection<Long> ids);
}
