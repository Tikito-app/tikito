package org.tikito.controller.user;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tikito.auth.LoggedInUserDto;
import org.tikito.controller.request.LoginRequest;
import org.tikito.service.UserAccountService;

@RestController
@RequestMapping("/api/user/login")
public class LoginController {
    private final UserAccountService userAccountService;

    public LoginController(final UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @PostMapping
    public ResponseEntity<LoggedInUserDto> login(@Valid @RequestBody final LoginRequest request) {
        return ResponseEntity.ok(userAccountService.login(request));
    }
}
