package com.project2.calendar.controller;

import com.project2.calendar.entity.User;
import com.project2.calendar.repository.UserRepository;
import com.project2.calendar.service.CurrentUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin", description = "Admin-only endpoints for managing users. Requires the caller to have isAdmin=true.")
public class AdminUserController {

    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    public AdminUserController(UserRepository userRepository, CurrentUserService currentUserService) {
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
    }

    @Operation(summary = "List all users", description = "Returns every user in the database. Caller must be an admin.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List returned successfully"),
        @ApiResponse(responseCode = "403", description = "Caller is not an admin")
    })
    @GetMapping
    public List<User> getAllUsers(@RequestHeader("X-User-Id") String supabaseId) {
        currentUserService.requireAdmin(supabaseId);
        return userRepository.findAll();
    }

    @Operation(summary = "Update admin status", description = "Grants or revokes admin privileges for a user. Admins cannot revoke their own access.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Admin status updated"),
        @ApiResponse(responseCode = "400", description = "Cannot remove your own admin access"),
        @ApiResponse(responseCode = "403", description = "Caller is not an admin"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
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

    @Operation(summary = "Test admin access", description = "Simple endpoint to verify the caller has admin privileges.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Caller is an admin"),
        @ApiResponse(responseCode = "403", description = "Caller is not an admin")
    })
    @GetMapping("/test")
    public String testAdmin(@RequestHeader("X-User-Id") String supabaseId) {
        currentUserService.requireAdmin(supabaseId);
        return "Admin check passed";
    }
}