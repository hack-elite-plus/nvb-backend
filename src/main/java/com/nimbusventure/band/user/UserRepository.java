package com.nimbusventure.band.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByEmail(String email);

    @Transactional
    @Modifying
    @Query("UPDATE User user " +
            "SET user.isVerified = TRUE WHERE user.id = ?1")
    int setUserVerified(Long userId);

    @Transactional
    @Modifying
    @Query("UPDATE User user " +
            "SET user.password = ?2 WHERE user.id = ?1")
    int updateUserPassword(Long userId, String newPassword);
}
