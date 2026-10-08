package com.occupify.notification.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to inject the authenticated user identity into controller method parameters.
 * Resolved from the X-User-Id header injected by the API Gateway, with fallback to userId query parameter.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Authenticated {

    /**
     * Whether the user must be authenticated. Defaults to true.
     */
    boolean required() default true;
}
