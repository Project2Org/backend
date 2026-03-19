package com.project2.calendar.service;

import com.project2.calendar.entity.Event;
import com.project2.calendar.entity.EventTag;
import com.project2.calendar.entity.Tag;
import com.project2.calendar.repository.EventRepository;
import com.project2.calendar.repository.EventTagRepository;
import com.project2.calendar.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventTagService {

    @Autowired
    private EventTagRepository eventTagRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TagRepository tagRepository;

    public List<EventTag> getTagsByEventId(Long eventId) {
        return eventTagRepository.findByEventId(eventId);
    }

    public EventTag addTagToEvent(Long eventId, Long tagId) {
        eventTagRepository.findByEventIdAndTagId(eventId, tagId).ifPresent(existing -> {
            throw new RuntimeException("Tag already added to this event");
        });

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found with id: " + tagId));

        EventTag eventTag = new EventTag();
        eventTag.setEvent(event);
        eventTag.setTag(tag);

        return eventTagRepository.save(eventTag);
    }

    public void removeTagFromEvent(Long eventId, Long tagId) {
        eventTagRepository.findByEventIdAndTagId(eventId, tagId)
                .orElseThrow(() -> new RuntimeException("Tag not found on this event"));

        eventTagRepository.deleteByEventIdAndTagId(eventId, tagId);
    }
}