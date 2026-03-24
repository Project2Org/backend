package com.project2.calendar.controller;

import com.project2.calendar.entity.Todo;
import com.project2.calendar.entity.User;
import com.project2.calendar.repository.TodoRepository;
import com.project2.calendar.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/todos")
@Tag(name = "Todos", description = "Manage daily to-do items for the authenticated user")
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
            u.setUsername(null);
            return userRepository.save(u);
        });
    }

    // GET /api/todos  or  GET /api/todos?date=YYYY-MM-DD
    @Operation(summary = "Get all todos", description = "Returns all todos for the authenticated user. Optionally filter by date (YYYY-MM-DD).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List returned successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
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
    @Operation(summary = "Create a todo", description = "Creates a new todo item for the authenticated user.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Todo created successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
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
    @Operation(summary = "Update a todo", description = "Partially updates a todo. Accepts `completed` (boolean) and/or `text` (string).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Todo updated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Todo not found or not owned by user")
    })
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
    @Operation(summary = "Delete a todo", description = "Deletes a todo by ID. Returns 404 if not found or owned by another user.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Todo deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Todo not found or not owned by user")
    })
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