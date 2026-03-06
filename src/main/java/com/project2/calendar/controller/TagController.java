package com.project2.calendar.controller;

import com.project2.calendar.entity.Tag;
import com.project2.calendar.entity.User;
import com.project2.calendar.repository.TagRepository;
import com.project2.calendar.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
public class TagController {

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private UserRepository userRepository;

    // Create Tag
    @PostMapping
    public ResponseEntity<Tag> createTag(@RequestBody Tag tag, @RequestParam Long userId) {
        User owner = userRepository.findById(userId)
                .orElse(null);
        if (owner == null) {
            return ResponseEntity.badRequest().build();
        }
        tag.setOwner(owner);
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

    // Retrieve all tags belonging to a user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Tag>> getTagsByUser(@PathVariable Long userId) {
        User owner = userRepository.findById(userId)
                .orElse(null);
        if (owner == null) {
            return ResponseEntity.notFound().build();
        }
        List<Tag> tags = tagRepository.findAllByOwner(owner);
        return ResponseEntity.ok(tags);
    }

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