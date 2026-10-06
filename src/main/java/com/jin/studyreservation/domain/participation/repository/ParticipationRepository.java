package com.jin.studyreservation.domain.participation.repository;

import com.jin.studyreservation.domain.participation.entity.Participation;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {

  boolean existsByMeeting_IdAndUser_Id(Long meetingId, Long userId);

  Optional<Participation> findByMeeting_IdAndUser_Id(Long meetingId, Long userId);

  long countByMeeting_Id(Long meetingId);

  @Query("select p.meeting.id from Participation p where p.user.id = :userId")
  List<Long> findMeetingIdsByUserId(@Param("userId") Long userId);
}
