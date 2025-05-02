package com.example.register.register.repository;

import com.example.register.register.model.BusinessProcess;
import com.example.register.register.model.Section;
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
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"role"})
    Optional<User> findByUsername(String username);

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

    @Query("SELECT u.team FROM User u WHERE u.username = :username")
    Team findTeamByUsername(@Param("username") String username);

    @Query("SELECT u.favoriteProcesses FROM User u WHERE u.id = :userId")
    Set<BusinessProcess> findFavoriteProcessesByUserId(@Param("userId") Long userId);

    // Pobieranie obecnych pracowników we WSZYSTKICH sekcjach
    @Query("SELECT u FROM User u WHERE u.id NOT IN " +
            "(SELECT a.user.id FROM Attendance a WHERE a.attendanceDate = CURRENT_DATE AND a.status = 'LEAVE')")
    List<User> findAllPresentEmployees();

    // Pobieranie pracowników na urlopie we WSZYSTKICH sekcjach
    @Query("SELECT u FROM User u WHERE u.id IN " +
            "(SELECT a.user.id FROM Attendance a WHERE a.attendanceDate = CURRENT_DATE AND a.status = 'LEAVE')")
    List<User> findAllOnLeaveEmployees();

    // Pobieranie obecnych pracowników w danej sekcji (np. nie są na urlopie)
    @Query("SELECT u FROM User u WHERE u.section.id = :sectionId AND u.id NOT IN (SELECT a.user.id FROM Attendance a WHERE a.attendanceDate = CURRENT_DATE AND a.status = 'leave')")
    List<User> findPresentEmployeesBySectionId(@Param("sectionId") Long sectionId);

    // Pobieranie pracowników na urlopie w danej sekcji
    @Query("SELECT u FROM User u WHERE u.section.id = :sectionId AND u.id IN (SELECT a.user.id FROM Attendance a WHERE a.attendanceDate = CURRENT_DATE AND a.status = 'leave')")
    List<User> findOnLeaveEmployeesBySectionId(@Param("sectionId") Long sectionId);

    @Query("SELECT COUNT(u) FROM User u WHERE u.section.id = :sectionId")
    long countBySectionId(@Param("sectionId") Long sectionId);

    @Query("SELECT u FROM User u")
    List<User> findAllEmployees();

    List<User> findBySection_Id(Long sectionId);
    List<User> findByTeamId(Long teamId);

}
