package com.jin.studyreservation.domain.user.repository;

import com.jin.studyreservation.domain.user.entity.Provider;
import com.jin.studyreservation.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByProviderAndProviderId(Provider provider, String providerId);
}
