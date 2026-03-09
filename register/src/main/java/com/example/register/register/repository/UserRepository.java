package com.example.register.register.repository;

import com.example.register.register.model.Team;
import com.example.register.register.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"role"})
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET id_role = ?2 WHERE id = ?1", nativeQuery = true)
    void updateUserRoleById(Long userId, int newRoleId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM users WHERE id = ?1", nativeQuery = true)
    void deleteUserById(Long userId);

    @Query("SELECT u.id FROM User u WHERE u.username = :username")
    Long findUserIdByUsername(@Param("username") String username);

    Optional<User> findByEmail(String email);

    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET section_id = ?2 WHERE id = ?1", nativeQuery = true)
    void updateUserSectionById(Long userId, Long sectionId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE users SET team_id = ?2 WHERE id = ?1", nativeQuery = true)
    void updateUserTeamById(Long userId, Long teamId);

    @Query("SELECT u.team FROM User u WHERE u.username = :username")
    Team findTeamByUsername(@Param("username") String username);

    @Query("SELECT u FROM User u")
    List<User> findAllEmployees();

    List<User> findBySection_Id(Long sectionId);
    List<User> findByTeamId(Long teamId);
    List<User> findAllByTeam(Team team);
    List<User> findByTeam_Id(Long teamId);

}
