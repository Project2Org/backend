package com.project2.calendar.controller;

import com.project2.calendar.entity.Calendar;
import com.project2.calendar.service.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendars")
@Tag(name = "Calendars", description = "Create, retrieve, and delete calendars")
public class CalendarController {

    @Autowired
    private CalendarService calendarService;

    // Create Calendar
    @Operation(summary = "Create a calendar", description = "Creates a new calendar.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Calendar created successfully")
    })
    @PostMapping
    public ResponseEntity<Calendar> createCalendar(@RequestBody Calendar calendar) {
        Calendar saved = calendarService.createCalendar(calendar);
        return ResponseEntity.ok(saved);
    }

    @Operation(summary = "Get all calendars", description = "Returns all calendars in the system.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List returned successfully")
    })
    @GetMapping
    public ResponseEntity<List<Calendar>> getAllCalendars() {
        List<Calendar> calendars = calendarService.getAllCalendars();
        return ResponseEntity.ok(calendars);
    }

    // Get Calendar by ID
    @Operation(summary = "Get calendar by ID", description = "Returns a calendar by its ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Calendar returned successfully"),
        @ApiResponse(responseCode = "404", description = "Calendar not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Calendar> getCalendarById(@PathVariable Long id) {
        Calendar calendar = calendarService.getCalendarById(id);
        return ResponseEntity.ok(calendar);
    }

    // Get all Calendars by owner
    @Operation(summary = "Get calendars by owner", description = "Returns all calendars belonging to a specific user by their internal user ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List returned successfully")
    })
    @GetMapping("/user/{ownerId}")
    public ResponseEntity<List<Calendar>> getCalendarsByOwner(@PathVariable Long ownerId) {
        List<Calendar> calendars = calendarService.getCalendarsByOwnerId(ownerId);
        return ResponseEntity.ok(calendars);
    }

    // Delete Calendar
    @Operation(summary = "Delete a calendar", description = "Deletes a calendar by its ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Calendar deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Calendar not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCalendar(@PathVariable Long id) {
        calendarService.deleteCalendar(id);
        return ResponseEntity.noContent().build();
    }
}