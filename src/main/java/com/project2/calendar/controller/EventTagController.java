package com.project2.calendar.controller;

import com.project2.calendar.entity.EventTag;
import com.project2.calendar.entity.User;
import com.project2.calendar.repository.CalendarRepository;
import com.project2.calendar.repository.EventRepository;
import com.project2.calendar.repository.UserRepository;
import com.project2.calendar.service.EventTagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventTagController {

    @Autowired
    private EventTagService eventTagService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CalendarRepository calendarRepository;

    private User currentUser(Jwt jwt) {
        String sub = jwt.getSubject();
        return userRepository.findBySupabaseId(sub).orElseGet(() -> {
            User u = new User();
            u.setSupabaseId(sub);
            u.setUsername(null);
            return userRepository.save(u);
        });
    }

    // GET /api/events/{id}/tags
    @GetMapping("/{id}/tags")
    public ResponseEntity<List<EventTag>> getTagsForEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        User owner = currentUser(jwt);
        boolean ownsEvent = eventRepository.findById(id)
                .map(e -> e.getCalendar().getOwner().getId().equals(owner.getId()))
                .orElse(false);

        if (!ownsEvent) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(eventTagService.getTagsByEventId(id));
    }

    // POST /api/events/{id}/tags
    // Body: { "tagId": 1 }
    @PostMapping("/{id}/tags")
    public ResponseEntity<EventTag> addTagToEvent(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body,
            @AuthenticationPrincipal Jwt jwt
    ) {
        User owner = currentUser(jwt);
        boolean ownsEvent = eventRepository.findById(id)
                .map(e -> e.getCalendar().getOwner().getId().equals(owner.getId()))
                .orElse(false);

        if (!ownsEvent) return ResponseEntity.notFound().build();

        Long tagId = body.get("tagId");
        return ResponseEntity.ok(eventTagService.addTagToEvent(id, tagId));
    }

    // DELETE /api/events/{id}/tags/{tagId}
    @DeleteMapping("/{id}/tags/{tagId}")
    public ResponseEntity<Void> removeTagFromEvent(
            @PathVariable Long id,
            @PathVariable Long tagId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        User owner = currentUser(jwt);
        boolean ownsEvent = eventRepository.findById(id)
                .map(e -> e.getCalendar().getOwner().getId().equals(owner.getId()))
                .orElse(false);

        if (!ownsEvent) return ResponseEntity.notFound().build();

        eventTagService.removeTagFromEvent(id, tagId);
        return ResponseEntity.noContent().build();
    }
}