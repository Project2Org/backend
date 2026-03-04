package com.project2.calendar.repository;

import com.project2.calendar.entity.Tag;
import com.project2.calendar.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    // Optional<> tells java that this may not exist
    Optional<Tag> findByName(String name);
    List<Tag> findAllByOwner(User owner);
}