package com.project2.calendar.controller;

import com.project2.calendar.entity.Calendar;
import com.project2.calendar.service.CalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/calendars")
public class CalendarController {

    @Autowired
    private CalendarService calendarService;

    // Create Calendar
    @PostMapping
    public ResponseEntity<Calendar> createCalendar(@RequestBody Calendar calendar) {
        Calendar saved = calendarService.createCalendar(calendar);
        return ResponseEntity.ok(saved);
    }

    // Get Calendar by ID
    @GetMapping("/{id}")
    public ResponseEntity<Calendar> getCalendarById(@PathVariable Long id) {
        Calendar calendar = calendarService.getCalendarById(id);
        return ResponseEntity.ok(calendar);
    }

    // Get all Calendars by owner
    @GetMapping("/user/{ownerId}")
    public ResponseEntity<List<Calendar>> getCalendarsByOwner(@PathVariable Long ownerId) {
        List<Calendar> calendars = calendarService.getCalendarsByOwnerId(ownerId);
        return ResponseEntity.ok(calendars);
    }

    // Delete Calendar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCalendar(@PathVariable Long id) {
        calendarService.deleteCalendar(id);
        return ResponseEntity.noContent().build();
    }
}