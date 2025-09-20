package org.tikito.controller;

import org.tikito.auth.AuthUser;
import org.tikito.auth.LoggedInUserDto;
import org.tikito.service.CacheService;
import org.tikito.service.UserAccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserAccountController {
    private final UserAccountService userAccountService;
    private final CacheService cacheService;

    public UserAccountController(final UserAccountService userAccountService,
                                 final CacheService cacheService) {
        this.userAccountService = userAccountService;
        this.cacheService = cacheService;
    }

    @GetMapping
    public ResponseEntity<LoggedInUserDto> getLoggedInUser(final AuthUser authUser) {
        return ResponseEntity.ok(userAccountService.getLoggedInUser(authUser));
    }

    @GetMapping("/initial-installation")
    public ResponseEntity<Boolean> isInitialSetup() {
        return ResponseEntity.ok(cacheService.isFirstEverUser());
    }
}
