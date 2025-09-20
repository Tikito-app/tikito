package org.tikito.controller;

import org.tikito.auth.AuthUser;
import org.tikito.dto.OverviewDto;
import org.tikito.service.OverviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/overview")
public class OverviewController {
    private final OverviewService overviewService;

    public OverviewController(final OverviewService overviewService) {
        this.overviewService = overviewService;
    }

    @GetMapping
    public ResponseEntity<OverviewDto> getOverview(final AuthUser user) {
        return ResponseEntity.ok(overviewService.getOverview(user.getId()));
    }
}
