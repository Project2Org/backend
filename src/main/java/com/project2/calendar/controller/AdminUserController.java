package com.project2.calendar.controller;

import com.project2.calendar.entity.User;
import com.project2.calendar.repository.UserRepository;
import com.project2.calendar.service.CurrentUserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public AdminUserController(UserRepository userRepository, CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public List<User> getAllUsers(@RequestHeader("X-User-Id") String supabaseId) {
        currentUserService.requireAdmin(supabaseId);
        return userRepository.findAll();
    }

    @PatchMapping("/{id}/admin")
    public User updateAdminStatus(
            @PathVariable Long id,
            @RequestParam boolean isAdmin,
            @RequestHeader("X-User-Id") String supabaseId
    ) {
        currentUserService.requireAdmin(supabaseId);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        user.setAdmin(isAdmin);
        if (user.getSupabaseId().equals(supabaseId) && !isAdmin) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot remove your own admin access");
        }
        return userRepository.save(user);
    }
    @GetMapping("/test")
    public String testAdmin(@RequestHeader("X-User-Id") String supabaseId) {
        currentUserService.requireAdmin(supabaseId);
        return "Admin check passed";
    }
}