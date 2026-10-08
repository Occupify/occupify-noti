package com.occupify.notification.config;

import com.occupify.notification.annotation.Authenticated;
import com.occupify.notification.exception.NotificationErrorCode;
import com.occupify.notification.exception.NotificationException;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.UUID;

@Component
public class AuthenticatedArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String PARAM_USER_ID = "userId";

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

        String rawUserId = webRequest.getHeader(HEADER_USER_ID);
        if (rawUserId == null || rawUserId.isBlank()) {
            rawUserId = webRequest.getParameter(PARAM_USER_ID);
        }

        if (rawUserId == null || rawUserId.isBlank()) {
            if (!required) {
                return null;
            }
            throw new NotificationException(NotificationErrorCode.FORBIDDEN,
                    "User authentication required: missing X-User-Id header");
        }

        rawUserId = rawUserId.trim();

        if (UUID.class.isAssignableFrom(parameter.getParameterType())) {
            try {
                return UUID.fromString(rawUserId);
            } catch (IllegalArgumentException ex) {
                throw new NotificationException(NotificationErrorCode.INVALID_PAYLOAD,
                        "Invalid UUID format for authenticated user ID: " + rawUserId);
            }
        }

        return rawUserId;
    }
}
