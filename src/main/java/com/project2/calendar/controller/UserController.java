package com.project2.calendar.controller;

import com.project2.calendar.entity.Calendar;
import com.project2.calendar.entity.Event;
import com.project2.calendar.entity.User;
import com.project2.calendar.repository.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Create, retrieve, and delete users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CalendarRepository calendarRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventTagRepository eventTagRepository;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private TagRepository tagRepository;

    // Create User
    @Operation(summary = "Create a user", description = "Creates a new user record.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User created successfully")
    })
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User saved = userRepository.save(user);
        return ResponseEntity.ok(saved);
    }

    // Retrieve User by ID
    @Operation(summary = "Get user by ID", description = "Returns a user by their internal database ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User returned successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Retrieve User by username
    @Operation(summary = "Get user by username", description = "Returns a user by their username.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User returned successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        return userRepository.findByUsername(username)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete User — cleans up all related data before deleting
    @Operation(summary = "Delete a user", description = "Deletes a user and all their related data (events, calendars, todos, tags).")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // Delete events and their tags for each calendar
        List<Calendar> calendars = calendarRepository.findByOwnerId(id);
        for (Calendar cal : calendars) {
            List<Event> events = eventRepository.findAllByCalendar(cal);
            for (Event event : events) {
                eventTagRepository.deleteByEventId(event.getId());
            }
            eventRepository.deleteAll(events);
        }
        calendarRepository.deleteAll(calendars);

        // Delete todos and tags
        todoRepository.deleteAll(todoRepository.findAllByOwner(user));
        tagRepository.deleteAll(tagRepository.findAllByOwner(user));

        // Finally delete the user
        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }

    // Old delete method (commented out for reference)
    // @DeleteMapping("/{id}")
    // public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
    //     if (!userRepository.existsById(id)) {
    //         return ResponseEntity.notFound().build();
    //     }
    //     userRepository.deleteById(id);
    //     return ResponseEntity.noContent().build();
    // }
}