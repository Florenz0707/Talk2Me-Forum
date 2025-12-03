package com.example.springboot_backend.talk2me.user.repository;

import com.example.springboot_backend.talk2me.user.model.domain.UserDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserDO, Long> {
    Optional<UserDO> findByUsername(String username);

    Optional<UserDO> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
