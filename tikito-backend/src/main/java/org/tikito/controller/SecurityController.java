package org.tikito.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.tikito.auth.AuthUser;
import org.tikito.dto.security.IsinDto;
import org.tikito.dto.security.SecurityDto;
import org.tikito.dto.security.SecurityPriceDto;
import org.tikito.dto.security.SecurityType;
import org.tikito.service.security.SecurityService;

import java.util.List;

@RestController
@RequestMapping("/api/security")
@Transactional
public class SecurityController {
    private final SecurityService securityService;

    public SecurityController(final SecurityService securityService) {
        this.securityService = securityService;
    }

    @GetMapping
    public ResponseEntity<List<SecurityDto>> getSecurities(final AuthUser authUser, @RequestParam(name = "type", required = false) final SecurityType type) {
        return ResponseEntity.ok(securityService.getSecurities(type));
    }

    @GetMapping("/{securityId}")
    public ResponseEntity<SecurityDto> getSecurity(final AuthUser authUser, @PathVariable("securityId") final long securityId) {
        return ResponseEntity.ok(securityService.getSecurity(securityId));
    }

    @GetMapping("/{securityId}/isins")
    public ResponseEntity<List<IsinDto>> getIsins(final AuthUser authUser, @PathVariable("id") final long securityId) {
        return ResponseEntity.ok(securityService.getIsins(securityId));
    }

    @GetMapping("/{securityId}/prices")
    public ResponseEntity<List<SecurityPriceDto>> getPrices(final AuthUser authUser, @PathVariable("id") final long securityId) {
        return ResponseEntity.ok(securityService.getPrices(securityId));
    }
}