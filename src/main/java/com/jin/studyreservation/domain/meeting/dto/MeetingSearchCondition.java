package com.jin.studyreservation.domain.meeting.dto;

import com.jin.studyreservation.domain.meeting.entity.Category;
import com.jin.studyreservation.domain.meeting.entity.MeetingStatus;

/** 모든 필드는 선택. null이면 조건을 걸지 않는다 */
public record MeetingSearchCondition(Category category, MeetingStatus status, String keyword) {

}
