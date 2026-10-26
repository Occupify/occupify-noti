package com.occupify.notification.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.occupify.notification.annotation.Authenticated;
import com.occupify.notification.entity.User;
import com.occupify.notification.exception.NotificationErrorCode;
import com.occupify.notification.exception.NotificationException;
import com.occupify.notification.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class AuthenticatedArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String PARAM_USER_ID = "userId";

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Value("${app.auth.dev-mock.enabled:false}")
    private boolean devMockEnabled;

    @Value("${app.auth.dev-mock.user-id:00000000-0000-0000-0000-000000000001}")
    private String devMockUserId;

    @Value("${app.auth.dev-mock.user-email:dev-employer@occupify.local}")
    private String devMockUserEmail;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(Authenticated.class)
                && (UUID.class.isAssignableFrom(parameter.getParameterType())
                || String.class.isAssignableFrom(parameter.getParameterType()));
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        Authenticated annotation = parameter.getParameterAnnotation(Authenticated.class);
        boolean required = annotation == null || annotation.required();
        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

        String rawUserId = request.getHeader(HEADER_USER_ID);
        if (!StringUtils.hasText(rawUserId)) {
            rawUserId = request.getParameter(PARAM_USER_ID);
        }

        // 1. Gateway header resolution (Production / Routed mode)
        if (StringUtils.hasText(rawUserId)) {
            UUID userId = parseUuid(rawUserId.trim());
            String email = request.getHeader(HEADER_USER_EMAIL);
            if (devMockEnabled) {
                ensureDevUserExists(userId, email);
            }
            return convertToParameterType(userId, parameter.getParameterType());
        }

        // 2. Direct Bearer token resolution (Scalar / API Documentation / Local Dev mode)
        String authHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String token = authHeader.substring(7).trim();
            ExtractedUser tokenUser = parseJwtPayload(token);
            if (tokenUser != null) {
                if (devMockEnabled) {
                    ensureDevUserExists(tokenUser.userId(), tokenUser.email());
                }
                return convertToParameterType(tokenUser.userId(), parameter.getParameterType());
            }
        }

        // 3. Fallback to mock user in dev environment
        if (devMockEnabled) {
            log.warn("[Security] Missing authentication headers. Dev mock mode is ACTIVE. Falling back to mock user: {}",
                    devMockUserId);
            UUID mockId = parseUuid(devMockUserId);
            ensureDevUserExists(mockId, devMockUserEmail);
            return convertToParameterType(mockId, parameter.getParameterType());
        }

        if (!required) {
            return null;
        }

        throw new NotificationException(NotificationErrorCode.FORBIDDEN,
                "User authentication required: missing X-User-Id header");
    }

    private Object convertToParameterType(UUID userId, Class<?> targetType) {
        if (UUID.class.isAssignableFrom(targetType)) {
            return userId;
        }
        return userId.toString();
    }

    private UUID parseUuid(String raw) {
        try {
            return UUID.fromString(raw.trim());
        } catch (IllegalArgumentException ex) {
            throw new NotificationException(NotificationErrorCode.INVALID_PAYLOAD,
                    "Invalid UUID format for authenticated user ID: " + raw);
        }
    }

    private record ExtractedUser(UUID userId, String email) {}

    private ExtractedUser parseJwtPayload(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            byte[] decodedBytes = Base64.getUrlDecoder().decode(parts[1]);
            String payloadJson = new String(decodedBytes, StandardCharsets.UTF_8);
            JsonNode jsonNode = objectMapper.readTree(payloadJson);

            String userIdStr = null;
            if (jsonNode.hasNonNull("userId")) {
                userIdStr = jsonNode.get("userId").asText();
            } else if (jsonNode.hasNonNull("sub")) {
                String sub = jsonNode.get("sub").asText();
                try {
                    UUID.fromString(sub);
                    userIdStr = sub;
                } catch (IllegalArgumentException ignored) {
                    // sub is not a UUID
                }
            }

            if (!StringUtils.hasText(userIdStr)) {
                log.warn("[Security] JWT payload does not contain a valid userId claim");
                return null;
            }

            UUID userId = UUID.fromString(userIdStr.trim());
            String email = "";
            if (jsonNode.hasNonNull("email")) {
                email = jsonNode.get("email").asText();
            } else if (jsonNode.hasNonNull("sub")) {
                email = jsonNode.get("sub").asText();
            }

            return new ExtractedUser(userId, email);
        } catch (Exception ex) {
            log.warn("[Security] Failed to decode JWT payload from Bearer token: {}", ex.getMessage());
            return null;
        }
    }

    private void ensureDevUserExists(UUID userId, String email) {
        if (!userRepository.existsById(userId)) {
            String resolvedEmail = StringUtils.hasText(email) ? email : "user-" + userId + "@occupify.local";
            log.info("[Security] Auto-provisioning mock development user [{}] ({})", userId, resolvedEmail);
            User devUser = User.builder()
                    .id(userId)
                    .email(resolvedEmail)
                    .fullName("Dev User " + (resolvedEmail.contains("@") ? resolvedEmail.substring(0, resolvedEmail.indexOf('@')) : userId.toString().substring(0, 8)))
                    .createdAt(LocalDateTime.now())
                    .build();
            userRepository.save(devUser);
        }
    }
}
