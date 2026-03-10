package com.project2.calendar.repository;

import com.project2.calendar.entity.Todo;
import com.project2.calendar.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findAllByOwner(User owner);

    List<Todo> findAllByOwnerAndDate(User owner, String date);
}
