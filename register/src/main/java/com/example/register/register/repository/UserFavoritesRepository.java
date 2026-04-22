package com.example.register.register.repository;

import com.example.register.register.model.User;
import com.example.register.register.model.UserFavorites;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFavoritesRepository extends JpaRepository<UserFavorites, Long> {
    void deleteByUser(User user);
}
