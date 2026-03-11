package com.project2.calendar;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project2.calendar.controller.TagController;
import com.project2.calendar.entity.Tag;
import com.project2.calendar.entity.User;
import com.project2.calendar.repository.TagRepository;
import com.project2.calendar.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TagController.class)
class TagControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    TagRepository tagRepository;

    @MockitoBean
    UserRepository userRepository;

    private static final String TEST_SUB = "test-supabase-uuid-1234";

    private Jwt mockJwt() {
        return Jwt.withTokenValue("mock-token")
                .header("alg", "ES256")
                .subject(TEST_SUB)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    private User makeUser() {
        User user = new User();
        user.setId(1L);
        user.setSupabaseId(TEST_SUB);
        return user;
    }

    private Tag makeTag(Long id, String name, User owner) {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(name);
        tag.setOwner(owner);
        return tag;
    }

    @Test
    void createTag_returnsNewTag() throws Exception {
        User owner = makeUser();
        Tag saved = makeTag(10L, "Work", owner);

        when(userRepository.findBySupabaseId(TEST_SUB)).thenReturn(Optional.of(owner));
        when(tagRepository.save(any(Tag.class))).thenReturn(saved);

        mockMvc.perform(post("/tags")
                        .with(jwt().jwt(mockJwt()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(makeTag(null, "Work", null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.name", is("Work")));
    }

    @Test
    void getTagById_returnsTag() throws Exception {
        User owner = makeUser();
        Tag tag = makeTag(10L, "Work", owner);

        when(userRepository.findBySupabaseId(TEST_SUB)).thenReturn(Optional.of(owner));
        when(tagRepository.findById(10L)).thenReturn(Optional.of(tag));

        mockMvc.perform(get("/tags/10")
                        .with(jwt().jwt(mockJwt())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.name", is("Work")));
    }

    @Test
    void getMyTags_returnsList() throws Exception {
        User owner = makeUser();
        List<Tag> tags = List.of(
                makeTag(10L, "Work", owner),
                makeTag(11L, "Personal", owner)
        );

        when(userRepository.findBySupabaseId(TEST_SUB)).thenReturn(Optional.of(owner));
        when(tagRepository.findAllByOwner(owner)).thenReturn(tags);

        mockMvc.perform(get("/tags/me")
                        .with(jwt().jwt(mockJwt())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", is("Work")))
                .andExpect(jsonPath("$[1].name", is("Personal")));
    }

    @Test
    void deleteTag_returnsNoContent() throws Exception {
        User owner = makeUser();
        Tag tag = makeTag(10L, "Work", owner);

        when(userRepository.findBySupabaseId(TEST_SUB)).thenReturn(Optional.of(owner));
        when(tagRepository.findById(10L)).thenReturn(Optional.of(tag));

        mockMvc.perform(delete("/tags/10")
                        .with(jwt().jwt(mockJwt())))
                .andExpect(status().isNoContent());
    }
}