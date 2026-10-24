package com.oliviya.tasktracker.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.oliviya.tasktracker.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByUserEmail(String email);

}
