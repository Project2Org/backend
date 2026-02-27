package com.project2.calendar.controller;

import com.project2.calendar.entity.Tag;
import com.project2.calendar.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tags")
public class TagController {

    @Autowired
    private TagRepository tagRepository;

    // Create Tag
    // TODO: Need user id to store in column
    @PostMapping
    public ResponseEntity<Tag> createTag(@RequestBody Tag tag) {
        Tag saved = tagRepository.save(tag);
        return ResponseEntity.ok(saved);
    }

    // Retrieve Tag by ID
    @GetMapping("/{id}")
    public ResponseEntity<Tag> getTagById(@PathVariable Long id) {
        return tagRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // TODO: Retrieve all tags created by a user
//    @GetMapping("/{ownerID}")
//    public ResponseEntity<Tag> findAllByUser(@PathVariable User user) {
//        return
//    }

    // Delete Tag
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        if (!tagRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        tagRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}