package com.project2.calendar.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "todos")
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String text;

    @Column(nullable = false)
    private boolean completed = false;

    @Column(nullable = false)
    private String date; // YYYY-MM-DD

    @ManyToOne
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
}