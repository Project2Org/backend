package com.project2.calendar.controller;

import com.project2.calendar.entity.Tag;
import com.project2.calendar.entity.User;
import com.project2.calendar.repository.TagRepository;
import com.project2.calendar.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tags")
public class TagController {

  @Autowired
  private TagRepository tagRepository;

  @Autowired
  private UserRepository userRepository;

  private User currentUser(Jwt jwt) {
    String sub = jwt.getSubject(); // Supabase user id (UUID)

    return userRepository.findBySupabaseId(sub).orElseGet(() -> {
      User u = new User();
      u.setSupabaseId(sub);
      return userRepository.save(u);
    });
  }

  @PostMapping
  public ResponseEntity<Tag> createTag(
      @RequestBody Tag tag,
      @AuthenticationPrincipal Jwt jwt
  ) {
    User owner = currentUser(jwt);
    tag.setOwner(owner);
    Tag saved = tagRepository.save(tag);
    return ResponseEntity.ok(saved);
  }

  @GetMapping("/{id}")
  public ResponseEntity<Tag> getTagById(
      @PathVariable Long id,
      @AuthenticationPrincipal Jwt jwt
  ) {
    User owner = currentUser(jwt);

    return tagRepository.findById(id)
        .filter(t -> t.getOwner() != null && t.getOwner().getId().equals(owner.getId()))
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/me")
  public ResponseEntity<List<Tag>> getMyTags(@AuthenticationPrincipal Jwt jwt) {
    User owner = currentUser(jwt);
    List<Tag> tags = tagRepository.findAllByOwner(owner);
    return ResponseEntity.ok(tags);
  }

  @DeleteMapping("/{id}")
public ResponseEntity<Void> deleteTag(
    @PathVariable Long id,
    @AuthenticationPrincipal Jwt jwt
) {
    User owner = currentUser(jwt);

    Tag tag = tagRepository.findById(id).orElse(null);

    if (tag == null || tag.getOwner() == null ||
        !tag.getOwner().getId().equals(owner.getId())) {
        return ResponseEntity.notFound().build();
    }

    tagRepository.delete(tag);
    return ResponseEntity.noContent().build();
    }
}