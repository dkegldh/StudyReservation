package com.jin.studyreservation.domain.participation.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.jin.studyreservation.domain.meeting.entity.Meeting;
import com.jin.studyreservation.domain.meeting.entity.MeetingStatus;
import com.jin.studyreservation.domain.meeting.repository.MeetingRepository;
import com.jin.studyreservation.domain.participation.repository.ParticipationRepository;
import com.jin.studyreservation.domain.user.entity.User;
import com.jin.studyreservation.domain.user.repository.UserRepository;
import com.jin.studyreservation.global.error.BusinessException;
import com.jin.studyreservation.global.error.ErrorCode;
import com.jin.studyreservation.support.Fixtures;
import com.jin.studyreservation.support.IntegrationTestSupport;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ParticipationConcurrencyTest extends IntegrationTestSupport {

  @Autowired
  private ParticipationFacade participationFacade;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private MeetingRepository meetingRepository;
  @Autowired
  private ParticipationRepository participationRepository;
  @Autowired
  private Clock clock;

  @Test
  void 정원_10명_모임에_100명이_동시에_신청해도_10명만_성공한다() throws InterruptedException {
    // given
    Meeting meeting = createMeeting(10);
    List<User> applicants = createUsers(100);
    AtomicInteger success = new AtomicInteger();
    Map<ErrorCode, AtomicInteger> failures = new ConcurrentHashMap<>();

    // when
    runConcurrently(applicants.size(), i -> {
      try {
        participationFacade.apply(meeting.getId(), applicants.get(i).getId());
        success.incrementAndGet();
      } catch (BusinessException e) {
        failures.computeIfAbsent(e.getErrorCode(), k -> new AtomicInteger()).incrementAndGet();
      }
    });

    // then
    Meeting result = meetingRepository.findById(meeting.getId()).orElseThrow();
    assertThat(success.get()).isEqualTo(10);
    assertThat(result.getCurrentParticipants()).isEqualTo(10);
    assertThat(participationRepository.countByMeeting_Id(meeting.getId())).isEqualTo(10);
    assertThat(result.getStatus()).isEqualTo(MeetingStatus.CLOSED);
    assertThat(failures.keySet()).containsOnly(ErrorCode.MEETING_FULL);
  }

  @Test
  void 같은_사용자가_동시에_여러_번_신청해도_한_번만_반영된다() throws InterruptedException {
    // given
    Meeting meeting = createMeeting(10);
    User user = createUsers(1).get(0);
    AtomicInteger success = new AtomicInteger();

    // when
    runConcurrently(20, i -> {
      try {
        participationFacade.apply(meeting.getId(), user.getId());
        success.incrementAndGet();
      } catch (BusinessException e) {
        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.ALREADY_PARTICIPATING);
      }
    });

    // then
    assertThat(success.get()).isEqualTo(1);
    assertThat(meetingRepository.findById(meeting.getId()).orElseThrow().getCurrentParticipants())
        .isEqualTo(1);
    assertThat(participationRepository.countByMeeting_Id(meeting.getId())).isEqualTo(1);
  }

  @Test
  void 신청과_취소가_동시에_섞여도_참여자_수와_신청_행_수가_일치하고_정원을_넘지_않는다()
      throws InterruptedException {
    // given: 정원 10명 중 10명이 이미 참여한 상태
    Meeting meeting = createMeeting(10);
    List<User> joined = createUsers(10);
    joined.forEach(user -> participationFacade.apply(meeting.getId(), user.getId()));
    List<User> newcomers = createUsers(30);

    // when: 기존 참여자 5명 취소와 새 사용자 30명 신청이 동시에 들어옴
    runConcurrently(35, i -> {
      try {
        if (i < 5) {
          participationFacade.cancel(meeting.getId(), joined.get(i).getId());
        } else {
          participationFacade.apply(meeting.getId(), newcomers.get(i - 5).getId());
        }
      } catch (BusinessException ignored) {
        // 정원 초과로 인한 신청 실패는 정상
      }
    });

    // then
    Meeting result = meetingRepository.findById(meeting.getId()).orElseThrow();
    long rows = participationRepository.countByMeeting_Id(meeting.getId());
    assertThat(result.getCurrentParticipants()).isEqualTo(rows);
    assertThat(result.getCurrentParticipants()).isLessThanOrEqualTo(10);
  }

  private Meeting createMeeting(int maxParticipants) {
    User host = userRepository.save(Fixtures.user("host"));
    return meetingRepository.save(
        Fixtures.meeting(host, maxParticipants, LocalDateTime.now(clock)));
  }

  private List<User> createUsers(int count) {
    List<User> users = new ArrayList<>();
    String prefix = String.valueOf(System.nanoTime());
    for (int i = 0; i < count; i++) {
      users.add(Fixtures.user(prefix + "-" + i));
    }
    return userRepository.saveAll(users);
  }

  /** 모든 스레드가 준비될 때까지 기다렸다가 동시에 출발시킨다 */
  private void runConcurrently(int count, IndexedTask task) throws InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(32);
    CountDownLatch ready = new CountDownLatch(count);
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch done = new CountDownLatch(count);
    List<Throwable> unexpected = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      int index = i;
      executor.submit(() -> {
        ready.countDown();
        try {
          start.await();
          task.run(index);
        } catch (Throwable t) {
          synchronized (unexpected) {
            unexpected.add(t);
          }
        } finally {
          done.countDown();
        }
      });
    }
    ready.await(10, TimeUnit.SECONDS);
    start.countDown();
    assertThat(done.await(60, TimeUnit.SECONDS)).isTrue();
    executor.shutdown();
    assertThat(unexpected).isEmpty();
  }

  @FunctionalInterface
  private interface IndexedTask {

    void run(int index) throws Exception;
  }
}
