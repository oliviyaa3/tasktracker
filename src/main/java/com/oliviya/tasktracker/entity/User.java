package com.oliviya.tasktracker.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    @Column(nullable = false)
    private String userName;
    @Column(nullable = false, unique = true)
    private String userEmail;

    private String userRole;

    @OneToMany(mappedBy = "assignee")
    private List<Task> tasks = new ArrayList<>();
}