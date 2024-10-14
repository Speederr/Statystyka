package com.example.register.register.repository;

import com.example.register.register.model.FormUsers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface FormUsersRepository extends JpaRepository<FormUsers, Long> {

    Optional<FormUsers> findByUsername(String username);

    @Modifying
    @Transactional
    @Query(value = "UPDATE form_users SET id_role = ?2 WHERE id = ?1", nativeQuery = true)
    void updateUserRoleById(Long userId, int newRoleId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM form_users WHERE id = ?1", nativeQuery = true)
    void deleteUserById(Long userId);

//    @Query(value = "SELECT f.id, f.first_name, f.last_name, f.username, f.email,f.password, f.avatar_url, f.first_login, f.id_role, f.role_name, r.roleName " +
//            "FROM form_users f " +
//            "JOIN roles r ON f.id_role = r.id", nativeQuery = true)
//    List<FormUsers> findAllWithRoles();



}
