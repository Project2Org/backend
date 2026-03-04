package com.project2.calendar.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project2.calendar.entity.Tag;
import com.project2.calendar.entity.User;
import com.project2.calendar.repository.TagRepository;
import com.project2.calendar.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
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

	private User makeUser(Long id, String username) {
		User user = new User();
		user.setId(id);
		user.setUsername(username);
		return user;
	}

	private Tag makeTag(Long id, String name, User owner) {
		Tag tag = new Tag();
		tag.setId(id);
		tag.setName(name);
		tag.setOwner(owner);
		return tag;
	}

	// should create and return the new tag
	@Test
	@DisplayName("POST /tags")
	void testCreateTag_success() throws Exception {
		User owner   = makeUser(1L, "alice");
		Tag incoming = makeTag(null, "Work", null); // owner set server-side
		Tag saved    = makeTag(10L, "Work", owner);

		when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
		when(tagRepository.save(any(Tag.class))).thenReturn(saved);

		mockMvc.perform(post("/tags")
						.param("userId", "1")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(incoming)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id",   is(10)))
				.andExpect(jsonPath("$.name", is("Work")));
	}

	// should return 400 when userId does not exist
	@Test
	@DisplayName("POST /tags")
	void testCreateTag_userNotFound() throws Exception {
		Tag incoming = makeTag(null, "Work", null);

		when(userRepository.findById(99L)).thenReturn(Optional.empty());

		mockMvc.perform(post("/tags")
						.param("userId", "99")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(incoming)))
				.andExpect(status().isBadRequest());
	}

	// should return the tag when found
	@Test
	@DisplayName("GET /tags/{id}")
	void testGetTagById_found() throws Exception {
		User owner = makeUser(1L, "alice");
		when(tagRepository.findById(10L)).thenReturn(Optional.of(makeTag(10L, "Work", owner)));

		mockMvc.perform(get("/tags/10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id",   is(10)))
				.andExpect(jsonPath("$.name", is("Work")));
	}

	// should return 404 when not found
	@Test
	@DisplayName("GET /tags/{id}")
	void testGetTagById_notFound() throws Exception {
		when(tagRepository.findById(99L)).thenReturn(Optional.empty());

		mockMvc.perform(get("/tags/99"))
				.andExpect(status().isNotFound());
	}

	// should return all tags for that user
	@Test
	@DisplayName("GET /tags/user/{userId}")
	void testGetTagsByUser_found() throws Exception {
		User owner = makeUser(1L, "alice");
		List<Tag> tags = Arrays.asList(
				makeTag(10L, "Work",     owner),
				makeTag(11L, "Personal", owner)
		);

		when(userRepository.findById(1L)).thenReturn(Optional.of(owner));
		when(tagRepository.findAllByOwner(owner)).thenReturn(tags);

		mockMvc.perform(get("/tags/user/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$",          hasSize(2)))
				.andExpect(jsonPath("$[0].name",  is("Work")))
				.andExpect(jsonPath("$[1].name",  is("Personal")));
	}

	// should return 404 when user does not exist
	@Test
	@DisplayName("GET /tags/user/{userId}")
	void testGetTagsByUser_userNotFound() throws Exception {
		when(userRepository.findById(99L)).thenReturn(Optional.empty());

		mockMvc.perform(get("/tags/user/99"))
				.andExpect(status().isNotFound());
	}

	// should return 204 when tag exists
	@Test
	@DisplayName("DELETE /tags/{id}")
	void testDeleteTag_found() throws Exception {
		when(tagRepository.existsById(10L)).thenReturn(true);

		mockMvc.perform(delete("/tags/10"))
				.andExpect(status().isNoContent());
	}

	// should return 404 when tag does not exist
	@Test
	@DisplayName("DELETE /tags/{id}")
	void testDeleteTag_notFound() throws Exception {
		when(tagRepository.existsById(99L)).thenReturn(false);

		mockMvc.perform(delete("/tags/99"))
				.andExpect(status().isNotFound());
	}
}
