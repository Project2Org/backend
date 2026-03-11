package com.project2.calendar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project2.calendar.entity.Todo;
import com.project2.calendar.entity.User;
import com.project2.calendar.repository.TodoRepository;
import com.project2.calendar.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    // A fake Supabase user ID used across all tests
    private static final String TEST_SUB = "test-supabase-uuid-1234";

    // Builds a mock JWT with the test subject
    private Jwt mockJwt() {
        return Jwt.withTokenValue("mock-token")
                .header("alg", "ES256")
                .subject(TEST_SUB)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    @BeforeEach
    void cleanUp() {
        // Delete todos first (FK constraint), then the test user
        User user = userRepository.findBySupabaseId(TEST_SUB).orElse(null);
        if (user != null) {
            todoRepository.deleteAll(todoRepository.findAllByOwner(user));
            userRepository.delete(user);
        }
    }

    // ─── Helper to create a todo directly in the DB ──────────────

    private Todo createTodoInDb(String text, String date, boolean completed) {
        User owner = userRepository.findBySupabaseId(TEST_SUB).orElseGet(() -> {
            User u = new User();
            u.setSupabaseId(TEST_SUB);
            return userRepository.save(u);
        });
        Todo todo = new Todo();
        todo.setText(text);
        todo.setDate(date);
        todo.setCompleted(completed);
        todo.setOwner(owner);
        return todoRepository.save(todo);
    }

    // ─── GET /api/todos ───────────────────────────────────────────

    @Test
    void getTodos_returnsEmptyList_whenNoTodos() throws Exception {
        mockMvc.perform(get("/api/todos")
                .with(jwt().jwt(mockJwt())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getTodos_returnsAllTodosForUser() throws Exception {
        createTodoInDb("Buy milk", "2026-03-10", false);
        createTodoInDb("Walk dog", "2026-03-10", false);

        mockMvc.perform(get("/api/todos")
                .with(jwt().jwt(mockJwt())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getTodos_filtersByDate() throws Exception {
        createTodoInDb("Today task",     "2026-03-10", false);
        createTodoInDb("Tomorrow task",  "2026-03-11", false);

        mockMvc.perform(get("/api/todos?date=2026-03-10")
                .with(jwt().jwt(mockJwt())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].text").value("Today task"));
    }

    // ─── POST /api/todos ──────────────────────────────────────────

    @Test
    void createTodo_persistsAndReturnsNewTodo() throws Exception {
        Map<String, Object> body = Map.of(
                "text", "New todo",
                "completed", false,
                "date", "2026-03-10"
        );

        mockMvc.perform(post("/api/todos")
                .with(jwt().jwt(mockJwt()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").value("New todo"))
                .andExpect(jsonPath("$.completed").value(false))
                .andExpect(jsonPath("$.date").value("2026-03-10"));
    }

    // ─── PATCH /api/todos/{id} ────────────────────────────────────

    @Test
    void patchTodo_updatesCompletedStatus() throws Exception {
        Todo todo = createTodoInDb("Finish tests", "2026-03-10", false);

        Map<String, Object> body = Map.of("completed", true);

        mockMvc.perform(patch("/api/todos/" + todo.getId())
                .with(jwt().jwt(mockJwt()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void patchTodo_returns404_whenNotFound() throws Exception {
        Map<String, Object> body = Map.of("completed", true);

        mockMvc.perform(patch("/api/todos/99999")
                .with(jwt().jwt(mockJwt()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    void patchTodo_returns404_whenOwnedByAnotherUser() throws Exception {
        // Create a todo under a different user
        User otherUser = userRepository.findBySupabaseId("other-user-uuid").orElseGet(() -> {
            User u = new User();
            u.setSupabaseId("other-user-uuid");
            return userRepository.save(u);
        });
        Todo todo = new Todo();
        todo.setText("Not yours");
        todo.setDate("2026-03-10");
        todo.setCompleted(false);
        todo.setOwner(otherUser);
        todoRepository.save(todo);

        Map<String, Object> body = Map.of("completed", true);

        // Try to patch it as our test user — should be blocked
        mockMvc.perform(patch("/api/todos/" + todo.getId())
                .with(jwt().jwt(mockJwt()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    // ─── DELETE /api/todos/{id} ───────────────────────────────────

    @Test
    void deleteTodo_removesFromDatabase() throws Exception {
        Todo todo = createTodoInDb("Delete me", "2026-03-10", false);

        mockMvc.perform(delete("/api/todos/" + todo.getId())
                .with(jwt().jwt(mockJwt())))
                .andExpect(status().isNoContent());

        // Verify it's actually gone from the DB
        assert todoRepository.findById(todo.getId()).isEmpty();
    }

    @Test
    void deleteTodo_returns404_whenNotFound() throws Exception {
        mockMvc.perform(delete("/api/todos/99999")
                .with(jwt().jwt(mockJwt())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteTodo_returns404_whenOwnedByAnotherUser() throws Exception {
        User otherUser = userRepository.findBySupabaseId("other-user-uuid-2").orElseGet(() -> {
            User u = new User();
            u.setSupabaseId("other-user-uuid-2");
            return userRepository.save(u);
        });
        Todo todo = new Todo();
        todo.setText("Not yours either");
        todo.setDate("2026-03-10");
        todo.setCompleted(false);
        todo.setOwner(otherUser);
        todoRepository.save(todo);

        mockMvc.perform(delete("/api/todos/" + todo.getId())
                .with(jwt().jwt(mockJwt())))
                .andExpect(status().isNotFound());
    }
}
