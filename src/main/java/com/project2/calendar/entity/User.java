package com.project2.calendar.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "supabase_id", nullable = false, unique = true)
    private String supabaseId;

    @Column(unique = true, nullable = true)
    private String username;

    @Column(name = "created_at")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSupabaseId() { return supabaseId; }
    public void setSupabaseId(String supabaseId) { this.supabaseId = supabaseId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    
    public boolean isAdmin() { return isAdmin; }

    public void setAdmin(boolean admin) { isAdmin = admin; }
}