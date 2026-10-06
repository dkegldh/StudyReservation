package com.jin.studyreservation.domain.user.entity;

import com.jin.studyreservation.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  /** 소셜 사용자는 이메일이 아니라 (provider, providerId)로 식별한다 */
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Provider provider;

  @Column(nullable = false, length = 100)
  private String providerId;

  @Column(nullable = false, length = 50)
  private String nickname;

  @Column(length = 500)
  private String profileImageUrl;

  private User(Provider provider, String providerId, String nickname, String profileImageUrl) {
    this.provider = provider;
    this.providerId = providerId;
    this.nickname = nickname;
    this.profileImageUrl = profileImageUrl;
  }

  public static User createSocialUser(Provider provider, String providerId, String nickname,
      String profileImageUrl) {
    return new User(provider, providerId, nickname, profileImageUrl);
  }

  /** 소셜 로그인 때마다 제공자 쪽 프로필로 동기화 */
  public void syncProfile(String nickname, String profileImageUrl) {
    this.nickname = nickname;
    this.profileImageUrl = profileImageUrl;
  }
}
