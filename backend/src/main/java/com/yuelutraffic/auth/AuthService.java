package com.yuelutraffic.auth;

import com.yuelutraffic.common.ApiException;
import com.yuelutraffic.user.AppUser;
import com.yuelutraffic.user.AppUserRepository;
import com.yuelutraffic.user.UserRole;
import com.yuelutraffic.user.UserSummary;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    public static final String PRIVACY_NOTICE =
            "Student number is used only to distinguish users inside Yuelu Traffic. It is not public and is not formal university identity verification.";

    private final AppUserRepository users;
    private final TokenService tokenService;
    private final String studentPepper;
    private final Set<String> adminStudentNumbers;

    public AuthService(
            AppUserRepository users,
            TokenService tokenService,
            @Value("${app.security.student-pepper}") String studentPepper,
            @Value("${app.security.admin-student-numbers:}") String adminStudentNumbers) {
        this.users = users;
        this.tokenService = tokenService;
        this.studentPepper = studentPepper;
        this.adminStudentNumbers = Arrays.stream(adminStudentNumbers.split(","))
                .map(AuthService::normalizeStudentNumber)
                .filter(value -> !value.isBlank())
                .collect(Collectors.toSet());
    }

    @Transactional
    public AuthResponse login(AuthRequest request) {
        String normalized = normalizeStudentNumber(request.studentNumber());
        String hash = hashStudentNumber(normalized);
        var existing = users.findByStudentNumberHash(hash);
        boolean newUser = existing.isEmpty();
        AppUser user = existing.orElseGet(() -> users.save(new AppUser(
                hash,
                createPublicCode(hash),
                adminStudentNumbers.contains(normalized) ? UserRole.ADMIN : UserRole.USER)));
        return new AuthResponse(tokenService.issueToken(user.getId()), UserSummary.from(user), newUser, PRIVACY_NOTICE);
    }

    @Transactional(readOnly = true)
    public AppUser requireUser(String authorizationHeader) {
        UUID userId = tokenService.parseBearerToken(authorizationHeader);
        return users.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Token user no longer exists"));
    }

    public static String normalizeStudentNumber(String rawStudentNumber) {
        return rawStudentNumber == null ? "" : rawStudentNumber.trim().toUpperCase(Locale.ROOT);
    }

    private String hashStudentNumber(String normalizedStudentNumber) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest((studentPepper + ":" + normalizedStudentNumber).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash student number", ex);
        }
    }

    private String createPublicCode(String hash) {
        String candidate = "User-" + hash.substring(0, 6).toUpperCase(Locale.ROOT);
        if (!users.existsByPublicCode(candidate)) {
            return candidate;
        }
        return "User-" + hash.substring(0, 10).toUpperCase(Locale.ROOT);
    }
}
