package com.project2.calendar.controller;

import com.project2.calendar.entity.Todo;
import com.project2.calendar.entity.User;
import com.project2.calendar.repository.TodoRepository;
import com.project2.calendar.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    private User currentUser(Jwt jwt) {
        String sub = jwt.getSubject();
        return userRepository.findBySupabaseId(sub).orElseGet(() -> {
            User u = new User();
            u.setSupabaseId(sub);
            return userRepository.save(u);
        });
    }

    // GET /api/todos  or  GET /api/todos?date=YYYY-MM-DD
    @GetMapping
    public ResponseEntity<List<Todo>> getTodos(
            @RequestParam(required = false) String date,
            @AuthenticationPrincipal Jwt jwt
    ) {
        User owner = currentUser(jwt);
        List<Todo> todos = (date != null)
                ? todoRepository.findAllByOwnerAndDate(owner, date)
                : todoRepository.findAllByOwner(owner);
        return ResponseEntity.ok(todos);
    }

    // POST /api/todos
    @PostMapping
    public ResponseEntity<Todo> createTodo(
            @RequestBody Todo todo,
            @AuthenticationPrincipal Jwt jwt
    ) {
        User owner = currentUser(jwt);
        todo.setOwner(owner);
        return ResponseEntity.ok(todoRepository.save(todo));
    }

    // PATCH /api/todos/{id}
    @PatchMapping("/{id}")
    public ResponseEntity<Todo> updateTodo(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates,
            @AuthenticationPrincipal Jwt jwt
    ) {
        User owner = currentUser(jwt);

        Todo todo = todoRepository.findById(id).orElse(null);
        if (todo == null || !todo.getOwner().getId().equals(owner.getId())) {
            return ResponseEntity.notFound().build();
        }

        if (updates.containsKey("completed")) {
            todo.setCompleted((Boolean) updates.get("completed"));
        }
        if (updates.containsKey("text")) {
            todo.setText((String) updates.get("text"));
        }

        return ResponseEntity.ok(todoRepository.save(todo));
    }

    // DELETE /api/todos/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTodo(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        User owner = currentUser(jwt);

        Todo todo = todoRepository.findById(id).orElse(null);
        if (todo == null || !todo.getOwner().getId().equals(owner.getId())) {
            return ResponseEntity.notFound().build();
        }

        todoRepository.delete(todo);
        return ResponseEntity.noContent().build();
    }
}