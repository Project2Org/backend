package com.project2.calendar;

import com.project2.calendar.entity.Calendar;
import com.project2.calendar.repository.CalendarRepository;
import com.project2.calendar.service.CalendarService;
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
public class CalendarServiceTests {

    @Mock
    private CalendarRepository calendarRepository;

    @InjectMocks
    private CalendarService calendarService;

    @Test
    void getAllCalendars_returnsListOfCalendars() {
        Calendar calendar = new Calendar();
        calendar.setId(1L);
        when(calendarRepository.findAll()).thenReturn(List.of(calendar));
        List<Calendar> result = calendarService.getAllCalendars();
        assertEquals(1, result.size());
        verify(calendarRepository, times(1)).findAll();
    }

    @Test
    void getCalendarById_returnsCalendar() {
        Calendar calendar = new Calendar();
        calendar.setId(1L);
        when(calendarRepository.findById(1L)).thenReturn(Optional.of(calendar));
        Calendar result = calendarService.getCalendarById(1L);
        assertEquals(1L, result.getId());
    }

    @Test
    void getCalendarById_throwsExceptionWhenNotFound() {
        when(calendarRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> calendarService.getCalendarById(99L));
    }

    @Test
    void createCalendar_savesAndReturnsCalendar() {
        Calendar calendar = new Calendar();
        when(calendarRepository.save(calendar)).thenReturn(calendar);
        Calendar result = calendarService.createCalendar(calendar);
        assertNotNull(result);
        verify(calendarRepository, times(1)).save(calendar);
    }

    @Test
    void deleteCalendar_callsDeleteById() {
        calendarService.deleteCalendar(1L);
        verify(calendarRepository, times(1)).deleteById(1L);
    }
}