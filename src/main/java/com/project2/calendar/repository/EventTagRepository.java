package com.project2.calendar.repository;

import com.project2.calendar.entity.EventTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventTagRepository extends JpaRepository<EventTag, Long> {

    List<EventTag> findByEventId(Long eventId);

    Optional<EventTag> findByEventIdAndTagId(Long eventId, Long tagId);

    void deleteByEventIdAndTagId(Long eventId, Long tagId);

    void deleteByEventId(Long eventId);
}