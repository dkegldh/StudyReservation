package com.jin.studyreservation.global.health;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jin.studyreservation.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

/** 배포 스크립트(deploy.sh)의 헬스체크가 이 응답의 "UP"에 의존한다 */
@AutoConfigureMockMvc
class HealthCheckTest extends IntegrationTestSupport {

  @Autowired
  private MockMvc mockMvc;

  @Test
  void 헬스체크는_DB와_Redis가_살아_있으면_UP을_응답한다() throws Exception {
    mockMvc.perform(get("/actuator/health"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UP"))
        .andExpect(jsonPath("$.components").doesNotExist());
  }

  @Test
  void 없는_API_경로는_404_NOT_FOUND로_응답한다() throws Exception {
    mockMvc.perform(get("/api/v1/unknown"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.code").value("NOT_FOUND"));
  }
}
