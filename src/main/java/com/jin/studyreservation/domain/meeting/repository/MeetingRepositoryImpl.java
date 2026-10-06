package com.jin.studyreservation.domain.meeting.repository;

import static com.jin.studyreservation.domain.meeting.entity.QMeeting.meeting;

import com.jin.studyreservation.domain.meeting.dto.MeetingSearchCondition;
import com.jin.studyreservation.domain.meeting.entity.Category;
import com.jin.studyreservation.domain.meeting.entity.Meeting;
import com.jin.studyreservation.domain.meeting.entity.MeetingStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class MeetingRepositoryImpl implements MeetingRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  /**
   * 아직 시작하지 않은 모임을 모임 시각이 가까운 순으로 조회한다.
   * 상태 조건은 화면 기준 상태({@link Meeting#statusAt})와 같게 맞춘다.
   */
  @Override
  public Page<Meeting> search(MeetingSearchCondition condition, LocalDateTime now,
      Pageable pageable) {
    BooleanExpression[] where = {
        meeting.meetingAt.gt(now),
        categoryEq(condition.category()),
        statusEq(condition.status(), now),
        keywordContains(condition.keyword())
    };

    List<Meeting> content = queryFactory
        .selectFrom(meeting)
        .where(where)
        .orderBy(meeting.meetingAt.asc(), meeting.id.asc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    JPAQuery<Long> countQuery = queryFactory
        .select(meeting.count())
        .from(meeting)
        .where(where);

    return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
  }

  private BooleanExpression categoryEq(Category category) {
    return category == null ? null : meeting.category.eq(category);
  }

  private BooleanExpression statusEq(MeetingStatus status, LocalDateTime now) {
    if (status == null) {
      return meeting.status.ne(MeetingStatus.CANCELED);
    }
    return switch (status) {
      case RECRUITING -> meeting.status.eq(MeetingStatus.RECRUITING)
          .and(meeting.recruitDeadline.gt(now));
      case CLOSED -> meeting.status.eq(MeetingStatus.CLOSED)
          .or(meeting.status.eq(MeetingStatus.RECRUITING).and(meeting.recruitDeadline.loe(now)));
      case CANCELED -> meeting.status.eq(MeetingStatus.CANCELED);
    };
  }

  private BooleanExpression keywordContains(String keyword) {
    if (!StringUtils.hasText(keyword)) {
      return null;
    }
    String trimmed = keyword.trim();
    return meeting.title.containsIgnoreCase(trimmed)
        .or(meeting.location.containsIgnoreCase(trimmed));
  }
}
