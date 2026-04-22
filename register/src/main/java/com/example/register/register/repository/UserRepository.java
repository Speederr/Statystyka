package com.example.register.register.repository;

import com.example.register.register.DTO.UserDto;
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
    boolean existsByEmail(String email);

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
    List<User> findByTeam_Id(Long teamId);

    @Query("""
    SELECT DISTINCT u
    FROM User u
    LEFT JOIN FETCH u.team t
    LEFT JOIN FETCH t.sections
    LEFT JOIN FETCH u.section
    LEFT JOIN FETCH u.role
    WHERE u.team = :team
""")
    List<User> findByTeamWithSections(@Param("team") Team team);

    @Query("""
    SELECT new com.example.register.register.DTO.UserDto(
        u.firstName,
        u.lastName,
        u.username,
        u.email,
        t.teamName,
        s.sectionName
    )
    FROM User u
    LEFT JOIN u.team t
    LEFT JOIN u.section s
    WHERE u.username = :username
""")
    Optional<UserDto> findUserProfileDtoByUsername(@Param("username") String username);

    @Query("""
    SELECT u
    FROM User u
    LEFT JOIN FETCH u.team
    WHERE u.id = :userId
""")
    Optional<User> findByIdWithTeam(@Param("userId") Long userId);

    @Query("""
    SELECT u
    FROM User u
    JOIN FETCH u.role
    JOIN FETCH u.position
    WHERE u.team.id = :teamId
""")
    List<User> findByTeamIdWithRoleAndPosition(@Param("teamId") Long teamId);

    @Query("""
    SELECT u
    FROM User u
    JOIN FETCH u.role
    LEFT JOIN FETCH u.position
    WHERE u.section.id = :sectionId
""")
    List<User> findBySectionIdWithRoleAndPosition(@Param("sectionId") Long sectionId);

    @Query("""
    SELECT u
    FROM User u
    JOIN FETCH u.role
    LEFT JOIN FETCH u.position
    WHERE u.id IN :ids
""")
    List<User> findAllByIdWithRoleAndPosition(@Param("ids") List<Long> ids);

    @Query("""
    SELECT u
    FROM User u
    JOIN FETCH u.role
    LEFT JOIN FETCH u.position
    WHERE u.id = :userId
""")
    Optional<User> findByIdWithRoleAndPosition(@Param("userId") Long userId);
}


