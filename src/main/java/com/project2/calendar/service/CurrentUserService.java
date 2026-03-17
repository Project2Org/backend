package com.project2.calendar.service;

import com.project2.calendar.entity.User;
import com.project2.calendar.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

   public User getUserBySupabaseId(String supabaseId) {
    return userRepository.findBySupabaseId(supabaseId)
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setSupabaseId(supabaseId);
                newUser.setAdmin(false);
                return userRepository.save(newUser);
            });
}

    public User requireAdmin(String supabaseId) {
        User user = getUserBySupabaseId(supabaseId);

        if (!user.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }

        return user;
    }
}