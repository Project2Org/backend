package com.project2.calendar.repository;

import com.project2.calendar.entity.Event;
import com.project2.calendar.entity.Calendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByCalendarId(Long calendarId);

    List<Event> findAllByCalendar(Calendar calendar);

    List<Event> findAllByCalendarAndDate(Calendar calendar, String date);
}