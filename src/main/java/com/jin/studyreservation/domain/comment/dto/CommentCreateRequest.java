package com.jin.studyreservation.domain.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** parentId가 있으면 답글 */
public record CommentCreateRequest(
    @NotBlank @Size(max = 1000) String content,
    Long parentId
) {

}
