package com.yuelutraffic.user;

import com.yuelutraffic.auth.AuthService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final AuthService authService;
    private final AppUserRepository users;

    public UserController(AuthService authService, AppUserRepository users) {
        this.authService = authService;
        this.users = users;
    }

    @GetMapping("/me")
    public UserSummary me(@RequestHeader("Authorization") String authorizationHeader) {
        return UserSummary.from(authService.requireUser(authorizationHeader));
    }

    @GetMapping("/leaderboard")
    public List<UserSummary> leaderboard() {
        return users.findTop50ByOrderByPointsDescReputationScoreDescCreatedAtAsc().stream()
                .map(UserSummary::from)
                .toList();
    }
}
