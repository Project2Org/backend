package com.project2.calendar.controller;

import com.project2.calendar.entity.Calendar;
import com.project2.calendar.entity.Event;
import com.project2.calendar.entity.User;
import com.project2.calendar.repository.CalendarRepository;
import com.project2.calendar.repository.EventRepository;
import com.project2.calendar.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
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

    // GET /api/events/{id}
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