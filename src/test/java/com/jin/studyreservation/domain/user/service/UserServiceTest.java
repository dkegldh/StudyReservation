package com.jin.studyreservation.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jin.studyreservation.domain.user.dto.MeResponse;
import com.jin.studyreservation.domain.user.entity.Provider;
import com.jin.studyreservation.domain.user.entity.User;
import com.jin.studyreservation.domain.user.repository.UserRepository;
import com.jin.studyreservation.global.error.ErrorCode;
import com.jin.studyreservation.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class UserServiceTest extends IntegrationTestSupport {

  @Autowired
  private UserService userService;
  @Autowired
  private UserRepository userRepository;

  @Test
  void 처음_소셜_로그인하면_가입된다() {
    // when
    User user = userService.loginSocialUser(Provider.KAKAO, "12345", "민준", "https://img/1");

    // then
    MeResponse me = userService.getMe(user.getId());
    assertThat(me.nickname()).isEqualTo("민준");
    assertThat(me.provider()).isEqualTo(Provider.KAKAO);
    assertThat(userRepository.count()).isEqualTo(1);
  }

  @Test
  void 다시_로그인하면_새로_가입하지_않고_프로필을_동기화한다() {
    // given
    User first = userService.loginSocialUser(Provider.KAKAO, "12345", "민준", null);

    // when
    User second = userService.loginSocialUser(Provider.KAKAO, "12345", "민준2", "https://img/2");

    // then
    assertThat(second.getId()).isEqualTo(first.getId());
    assertThat(userService.getMe(first.getId()).nickname()).isEqualTo("민준2");
    assertThat(userRepository.count()).isEqualTo(1);
  }

  @Test
  void 같은_providerId라도_제공자가_다르면_다른_사용자다() {
    User kakao = userService.loginSocialUser(Provider.KAKAO, "same", "카카오", null);
    User google = userService.loginSocialUser(Provider.GOOGLE, "same", "구글", null);

    assertThat(kakao.getId()).isNotEqualTo(google.getId());
  }

  @Test
  void 없는_사용자는_조회할_수_없다() {
    assertThatThrownBy(() -> userService.getMe(999L))
        .extracting("errorCode").isEqualTo(ErrorCode.USER_NOT_FOUND);
  }
}
