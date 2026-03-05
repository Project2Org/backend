package com.project2.calendar.service;

import com.project2.calendar.entity.Calendar;
import com.project2.calendar.repository.CalendarRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CalendarService {

    @Autowired
    private CalendarRepository calendarRepository;

    public List<Calendar> getAllCalendars() {
        return calendarRepository.findAll();
    }

    public Calendar getCalendarById(Long id) {
        return calendarRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Calendar not found with id: " + id));
    }

    public List<Calendar> getCalendarsByOwnerId(Long ownerId) {
        return calendarRepository.findByOwnerId(ownerId);
    }

    public Calendar createCalendar(Calendar calendar) {
        return calendarRepository.save(calendar);
    }

    public void deleteCalendar(Long id) {
        calendarRepository.deleteById(id);
    }
}