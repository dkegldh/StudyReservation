package com.jin.studyreservation.global.error;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jin.studyreservation.domain.meeting.entity.Category;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();
  }

  @Test
  void 비즈니스_예외는_ErrorCode의_상태와_메시지로_응답한다() throws Exception {
    mockMvc.perform(get("/test/business"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.code").value("MEETING_FULL"))
        .andExpect(jsonPath("$.message").value("방금 정원이 모두 찼어요."));
  }

  @Test
  void 요청_본문_검증에_실패하면_400_INVALID_INPUT으로_응답한다() throws Exception {
    mockMvc.perform(post("/test/body")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"title\": \"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_INPUT"))
        .andExpect(jsonPath("$.message").value("입력값을 다시 확인해 주세요."));
  }

  @Test
  void 잘못된_JSON이면_400_INVALID_INPUT으로_응답한다() throws Exception {
    mockMvc.perform(post("/test/body")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{not json"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
  }

  @Test
  void 없는_enum_값이나_필수_파라미터_누락은_400_INVALID_INPUT으로_응답한다() throws Exception {
    mockMvc.perform(get("/test/param").param("category", "UNKNOWN"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    mockMvc.perform(get("/test/param"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
  }

  @Test
  void 지원하지_않는_메서드는_405로_응답한다() throws Exception {
    mockMvc.perform(delete("/test/business"))
        .andExpect(status().isMethodNotAllowed())
        .andExpect(jsonPath("$.code").value("METHOD_NOT_ALLOWED"));
  }

  @Test
  void 예상하지_못한_예외는_내부_메시지를_숨기고_500으로_응답한다() throws Exception {
    mockMvc.perform(get("/test/unexpected"))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
        .andExpect(jsonPath("$.message").value("잠시 후 다시 시도해 주세요."));
  }

  /** 테스트 클래스 안에 중첩해 두면 다른 @SpringBootTest의 컴포넌트 스캔에서 제외된다 */
  @RestController
  static class TestController {

    @GetMapping("/test/business")
    void business() {
      throw new BusinessException(ErrorCode.MEETING_FULL);
    }

    @PostMapping("/test/body")
    void body(@Valid @RequestBody TestRequest request) {
    }

    @GetMapping("/test/param")
    void param(@RequestParam Category category) {
    }

    @GetMapping("/test/unexpected")
    void unexpected() {
      throw new IllegalStateException("internal detail: db password=secret");
    }
  }

  record TestRequest(@NotBlank String title) {

  }
}
