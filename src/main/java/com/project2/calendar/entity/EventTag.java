package com.project2.calendar.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "event_tags", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"event_id", "tag_id"})
})
public class EventTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @JoinColumn(name = "tag_id")
    private Tag tag;

    /*=================
    Getters and Setters
    =================*/

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }

    public Tag getTag() { return tag; }
    public void setTag(Tag tag) { this.tag = tag; }
}