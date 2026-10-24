package com.oliviya.tasktracker.entity;

import java.time.LocalDate;

import com.oliviya.tasktracker.enums.Priority;
import com.oliviya.tasktracker.enums.Status;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long taskId;
    @Column(nullable = false)
    private String taskTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status taskStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority taskPriority;

    private LocalDate taskDueDate;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne
    @JoinColumn(name = "assigneeId")
    private User assignee;

}
