package com.project2.calendar.controller;

import com.project2.calendar.entity.EventTag;
import com.project2.calendar.entity.User;
import com.project2.calendar.repository.CalendarRepository;
import com.project2.calendar.repository.EventRepository;
import com.project2.calendar.repository.UserRepository;
import com.project2.calendar.service.EventTagService;
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
@RequestMapping("/api/events")
@Tag(name = "Event Tags", description = "Assign and remove tags on events")
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
    @Operation(summary = "Get tags for an event", description = "Returns all tags attached to the specified event. Returns 404 if the event doesn't belong to the user.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tags returned successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Event not found or not owned by user")
    })
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
    @Operation(summary = "Add a tag to an event", description = "Attaches a tag to an event. Request body: { \"tagId\": 1 }.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tag added successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Event not found or not owned by user")
    })
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
    @Operation(summary = "Remove a tag from an event", description = "Detaches a tag from an event. Returns 404 if the event doesn't belong to the user.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Tag removed successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Event not found or not owned by user")
    })
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