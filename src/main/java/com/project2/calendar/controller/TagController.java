package com.project2.calendar.controller;

import com.project2.calendar.entity.Tag;
import com.project2.calendar.entity.User;
import com.project2.calendar.repository.TagRepository;
import com.project2.calendar.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tags", description = "Create and manage event tags for the authenticated user")
public class TagController {

  @Autowired
  private TagRepository tagRepository;

  @Autowired
  private UserRepository userRepository;

  private User currentUser(Jwt jwt) {
    String sub = jwt.getSubject();
    return userRepository.findBySupabaseId(sub).orElseGet(() -> {
        User u = new User();
        u.setSupabaseId(sub);
        u.setUsername("");  // satisfy the not-null constraint
        return userRepository.save(u);
    });
  }

  @Operation(summary = "Create a tag", description = "Creates a new tag owned by the authenticated user.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Tag created successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
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

    // PUT /api/tags/{id}
    @Operation(summary = "Update a tag", description = "Replaces a tag entirely.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Tag updated successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Tag not found or not owned by user")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Tag> updateTag(
            @PathVariable Long id,
            @RequestBody Tag updated,
            @AuthenticationPrincipal Jwt jwt
    ) {
        User owner = currentUser(jwt);

        Tag tag = tagRepository.findById(id).orElse(null);
        if (tag == null || tag.getOwner() == null ||
                !tag.getOwner().getId().equals(owner.getId())) {
            return ResponseEntity.notFound().build();
        }

        tag.setName(updated.getName());

        return ResponseEntity.ok(tagRepository.save(tag));
    }

  @Operation(summary = "Get tag by ID", description = "Returns a tag by ID. Returns 404 if not found or owned by another user.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "Tag returned successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "Tag not found or not owned by user")
  })
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

  @Operation(summary = "Get my tags", description = "Returns all tags belonging to the authenticated user.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "List returned successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized")
  })
  @GetMapping("/me")
  public ResponseEntity<List<Tag>> getMyTags(@AuthenticationPrincipal Jwt jwt) {
    User owner = currentUser(jwt);
    List<Tag> tags = tagRepository.findAllByOwner(owner);
    return ResponseEntity.ok(tags);
  }

  @Operation(summary = "Delete a tag", description = "Deletes a tag by ID. Returns 404 if not found or owned by another user.")
  @ApiResponses({
      @ApiResponse(responseCode = "204", description = "Tag deleted successfully"),
      @ApiResponse(responseCode = "401", description = "Unauthorized"),
      @ApiResponse(responseCode = "404", description = "Tag not found or not owned by user")
  })
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