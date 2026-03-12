package com.project2.calendar;

import com.project2.calendar.entity.Event;
import com.project2.calendar.repository.EventRepository;
import com.project2.calendar.service.EventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTests {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    void getAllEvents_returnsListOfEvents() {
        Event event = new Event();
        event.setId(1L);
        when(eventRepository.findAll()).thenReturn(List.of(event));
        List<Event> result = eventService.getAllEvents();
        assertEquals(1, result.size());
        verify(eventRepository, times(1)).findAll();
    }

    @Test
    void getEventById_returnsEvent() {
        Event event = new Event();
        event.setId(1L);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        Event result = eventService.getEventById(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    void getEventById_throwsExceptionWhenNotFound() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> eventService.getEventById(99L));
    }

    @Test
    void getEventsByCalendarId_returnsListOfEvents() {
        Event event = new Event();
        event.setId(1L);
        when(eventRepository.findByCalendarId(1L)).thenReturn(List.of(event));
        List<Event> result = eventService.getEventsByCalendarId(1L);
        assertEquals(1, result.size());
        verify(eventRepository, times(1)).findByCalendarId(1L);
    }

    @Test
    void createEvent_savesAndReturnsEvent() {
        Event event = new Event();
        when(eventRepository.save(event)).thenReturn(event);
        Event result = eventService.createEvent(event);
        assertNotNull(result);
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void deleteEvent_callsDeleteById() {
        eventService.deleteEvent(1L);
        verify(eventRepository, times(1)).deleteById(1L);
    }
}