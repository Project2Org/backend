package com.project2.calendar.controller;

import com.project2.calendar.entity.Calendar;
import com.project2.calendar.entity.Event;
import com.project2.calendar.entity.User;
import com.project2.calendar.repository.CalendarRepository;
import com.project2.calendar.repository.EventRepository;
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

@RestController
@RequestMapping("/api/events")
@Tag(name = "Events", description = "Create, retrieve, and delete calendar events for the authenticated user")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CalendarRepository calendarRepository;

    /**
     * Resolves the current user from the JWT, creating a User row if this is
     * their first request (same pattern as TodoController / TagController).
     */
    private User currentUser(Jwt jwt) {
        String sub = jwt.getSubject();
        return userRepository.findBySupabaseId(sub).orElseGet(() -> {
            User u = new User();
            u.setSupabaseId(sub);
            u.setUsername(null);
            return userRepository.save(u);
        });
    }

    /**
     * Each user gets exactly one default Calendar.  We create it lazily on the
     * first event operation so no explicit "create calendar" step is needed.
     */
    private Calendar defaultCalendar(User owner) {
        List<Calendar> existing = calendarRepository.findByOwnerId(owner.getId());
        if (!existing.isEmpty()) {
            return existing.get(0);
        }
        Calendar cal = new Calendar();
        cal.setOwner(owner);
        return calendarRepository.save(cal);
    }

    // GET /api/events
    // Date can be used in query string YYYY-MM-DD
    @Operation(summary = "Get all events", description = "Returns all events for the authenticated user. Optionally filter by date (YYYY-MM-DD).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List returned successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping
    public ResponseEntity<List<Event>> getEvents(
            @RequestParam(required = false) String date,
            @AuthenticationPrincipal Jwt jwt
    ) {
        User owner = currentUser(jwt);
        Calendar cal = defaultCalendar(owner);

        List<Event> events = (date != null)
                ? eventRepository.findAllByCalendarAndDate(cal, date)
                : eventRepository.findAllByCalendar(cal);

        return ResponseEntity.ok(events);
    }

    // POST /api/events
    @Operation(summary = "Create an event", description = "Creates a new event attached to the authenticated user's default calendar.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event created successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping
    public ResponseEntity<Event> createEvent(
            @RequestBody Event event,
            @AuthenticationPrincipal Jwt jwt
    ) {
        User owner = currentUser(jwt);
        Calendar cal = defaultCalendar(owner);
        event.setCalendar(cal);
        return ResponseEntity.ok(eventRepository.save(event));
    }

    // PUT /api/events/{id}
    @Operation(summary = "Update an event", description = "Replaces an event entirely.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event updated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Event not found or not owned by user")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(
            @PathVariable Long id,
            @RequestBody Event updated,
            @AuthenticationPrincipal Jwt jwt
    ) {
        User owner = currentUser(jwt);
        Calendar cal = defaultCalendar(owner);

        Event event = eventRepository.findById(id).orElse(null);
        if (event == null || event.getCalendar() == null ||
                !event.getCalendar().getId().equals(cal.getId())) {
            return ResponseEntity.notFound().build();
        }

        event.setTitle(updated.getTitle());
        event.setDate(updated.getDate());
        event.setTime(updated.getTime());
        event.setEndTime(updated.getEndTime());
        event.setDescription(updated.getDescription());
        event.setLocation(updated.getLocation());

        return ResponseEntity.ok(eventRepository.save(event));
    }


    // GET /api/events/{id}
    @Operation(summary = "Get event by ID", description = "Returns a single event by ID. Returns 404 if not found or owned by another user.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Event returned successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Event not found or not owned by user")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        User owner = currentUser(jwt);
        Calendar cal = defaultCalendar(owner);

        return eventRepository.findById(id)
                .filter(e -> e.getCalendar() != null &&
                             e.getCalendar().getId().equals(cal.getId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/events/{id}
    @Operation(summary = "Delete an event", description = "Deletes an event by ID. Returns 404 if not found or owned by another user.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Event deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Event not found or not owned by user")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        User owner = currentUser(jwt);
        Calendar cal = defaultCalendar(owner);

        Event event = eventRepository.findById(id).orElse(null);
        if (event == null || event.getCalendar() == null ||
                !event.getCalendar().getId().equals(cal.getId())) {
            return ResponseEntity.notFound().build();
        }

        eventRepository.delete(event);
        return ResponseEntity.noContent().build();
    }
}