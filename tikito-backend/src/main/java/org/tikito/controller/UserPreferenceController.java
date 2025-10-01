package org.tikito.controller;

import org.springframework.validation.annotation.Validated;
import org.tikito.auth.AuthUser;
import org.tikito.controller.request.SetUserPreferenceRequest;
import org.tikito.dto.UserPreferenceKey;
import org.tikito.service.UserPreferenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user-preference")
@Transactional
public class UserPreferenceController {
    private final UserPreferenceService userPreferenceService;

    public UserPreferenceController(final UserPreferenceService userPreferenceService) {
        this.userPreferenceService = userPreferenceService;
    }

    @GetMapping
    public ResponseEntity<Map<UserPreferenceKey, Object>> getAllUserPreferences(final AuthUser authUser) {
        return ResponseEntity.ok(userPreferenceService.getAllUserPreferences(authUser.getId()));
    }

    @PostMapping
    public ResponseEntity<Void> setUserPreference(final AuthUser authUser, @Validated @RequestBody final SetUserPreferenceRequest request) {
        userPreferenceService.setUserPreference(authUser.getId(), request.getKey(), request.getValue());
        return ResponseEntity.ok().build();
    }
}
