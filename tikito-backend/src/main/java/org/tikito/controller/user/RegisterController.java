package org.tikito.controller.user;

import org.springframework.validation.annotation.Validated;
import org.tikito.controller.request.RegisterRequest;
import org.tikito.exception.EmailAlreadyExistsException;
import org.tikito.exception.PasswordNotLongEnoughException;
import org.tikito.service.CacheService;
import org.tikito.service.UserAccountService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/user/register")
public class RegisterController {
    private final UserAccountService userService;
    private final CacheService cacheService;

    public RegisterController(final UserAccountService userService,
                              final CacheService cacheService) {
        this.userService = userService;
        this.cacheService = cacheService;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<Void> register(@Validated @RequestBody final RegisterRequest request, final HttpServletRequest httpServletRequest) throws EmailAlreadyExistsException, PasswordNotLongEnoughException, IOException {
        userService.register(request.getEmail(), request.getPassword());
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Boolean> isFirstEverUser() {
        return ResponseEntity.ok(cacheService.isFirstEverUser());
    }
}
