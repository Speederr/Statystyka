package com.example.register.register.service;

import com.example.register.register.model.FormUsers;
import com.example.register.register.model.User;
import com.example.register.register.repository.FormUsersRepository;
import com.example.register.register.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FormUsersRepository formUsersRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EntityManager entityManager;

    public void createUser(String firstName, String lastName, String username, String email, Long roleId) {

        String temporaryPassword = generateTemporaryPassword();
        // Tworzenie obiektu użytkownika
//        User user = new User();
        FormUsers user = new FormUsers();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setEmail(email);
        String hashedPassword = passwordEncoder.encode(temporaryPassword);
        user.setPassword(hashedPassword);

        // Ustaw wartość first_login na true
        user.setFirst_login(true);
        // Ustaw id_role na wartość przekazaną jako argument
        user.setId_role(roleId);
        // Zapisz użytkownika w bazie danych
            formUsersRepository.save(user);

        // Wysłanie e-maila z informacją o utworzeniu użytkownika
        emailService.sendUserCreationMail(
                email,          // Adres e-mail odbiorcy
                firstName,      // Imię użytkownika
                lastName,       // Nazwisko użytkownika
                username,       // Login użytkownika
                temporaryPassword  // Tymczasowe hasło użytkownika
        );
    }

    public String generateTemporaryPassword() {
        // Możesz użyć bardziej złożonego algorytmu, jeśli to konieczne
        return UUID.randomUUID().toString().substring(0, 8); // Generuje losowe hasło o długości 8 znaków
    }

    // Metoda do zapisywania użytkownika
    public void saveUser(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        userRepository.save(user);
    }

//     Implementacja metody UserDetailsService do ładowania użytkownika przez nazwę użytkownika
//    @Override
//    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//        System.out.println("Trying to load user with username: " + username);
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
//
//        System.out.println("User found: " + user.getUsername());
//
//        return org.springframework.security.core.userdetails.User.builder()
//                .username(user.getUsername())
//                .password(user.getPassword())
//                .build();
//    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("Trying to load user with username: " + username);
        FormUsers user = formUsersRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found in form_users."));

        System.out.println("User found: " + user.getUsername());

        // Create and return Spring Security User
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .build();

    }
//
//    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(int id_role) {
//        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
//    }


    //    public List<FormUsers> getAllUsersWithRoles() {
//        String sql = "SELECT form_users.id, form_users.first_name, form_users.last_name, form_users.username, form_users.email, roles.role_name " +
//                "FROM form_users JOIN roles ON form_users.id_role = roles.id";
//        return jdbcTemplate.query(sql, BeanPropertyRowMapper.newInstance(FormUsers.class));
////        return formUsersRepository.findAllWithRoles();
//    }
    public List<FormUsers> getAllUsersWithRoles() {
        String sql = "SELECT form_users.id, form_users.first_name, form_users.last_name, form_users.username, form_users.email, roles.role_name " +
                "FROM form_users JOIN roles ON form_users.id_role = roles.id";

        return jdbcTemplate.query(sql, new RowMapper<FormUsers>() {
            @Override
            public FormUsers mapRow(ResultSet rs, int rowNum) throws SQLException {
                FormUsers user = new FormUsers();
                user.setId(rs.getLong("id"));
                user.setFirstName(rs.getString("first_name"));
                user.setLastName(rs.getString("last_name"));
                user.setUsername(rs.getString("username"));
                user.setEmail(rs.getString("email"));
                user.setRole_name(rs.getString("role_name"));
                return user;
            }
        });
    }


    public void saveUserForm(List<FormUsers> users) {
        String sql = "INSERT INTO form_users(first_name, last_name, username, email, id_role, first_login) VALUES(?, ?, ?, ?, ?, ?)";
        users.forEach(user -> jdbcTemplate.update(sql, user.getFirstName(), user.getLastName(), user.getUsername(), user.getEmail(), user.getId_role(), true));
    }

    @Transactional
    public void updateUserRole(Long userId, int newRoleId) {
        // Zaktualizuj rolę użytkownika w bazie danych
        formUsersRepository.updateUserRoleById(userId, newRoleId);
        System.out.println("Updated role for userId: " + userId + " to roleId " + newRoleId);
    }

    @Transactional
    public void deleteUserById(Long userId) {
        // Retrieve the username from form_users based on userId
        Optional<FormUsers> user = formUsersRepository.findById(userId);

        if (user.isPresent()) {
            String username = user.get().getUsername();
            formUsersRepository.deleteUserById(userId);
        } else {
            throw new EntityNotFoundException("User with id " + userId + " not found.");
        }
    }

//    @Transactional
//    public boolean isFirstLogin(String username) {
//        User user = userRepository.findByUsername(username)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
//        return user.isFirst_login();
//    }
    @Transactional
    public boolean isFirstLogin(String username) {
        // Query the form_users table using formUsersRepository
        FormUsers user = formUsersRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found in form_users."));

        // Return the first_login status from the form_users table
        return user.isFirst_login();
    }


    @Transactional
    public void updateFirstLoginStatus(String username) {
        FormUsers user = formUsersRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setFirst_login(false);
        formUsersRepository.save(user);
    }

    @Transactional
    public void updateUserPassword(String username, String newPassword) {
        FormUsers user = formUsersRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        formUsersRepository.save(user);
    }

    public Optional<FormUsers> findByUsername(String username) {
        return formUsersRepository.findByUsername(username);
    }

}
