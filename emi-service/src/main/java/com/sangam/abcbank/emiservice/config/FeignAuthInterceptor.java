package com.sangam.abcbank.emiservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Propagates the caller's "Authorization: Bearer <jwt>" header from the incoming
 * request onto every outgoing Feign call to loan-service / banking-service.
 * This is required so that:
 *  - loan-service / banking-service can independently enforce their own security,
 *  - the "same username or admin" check in loan-service (if it does its own check)
 *    sees the original caller's identity, not the emi-service's own identity.
 */
@Component
public class FeignAuthInterceptor implements RequestInterceptor {

    private static final String AUTH_HEADER = "Authorization";

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            // No inbound HTTP request in context (e.g. called from the scheduled job).
            // The scheduler uses a service-to-service token instead - see AutoPaymentScheduler.
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader != null) {
            template.header(AUTH_HEADER, authHeader);
        }
    }
}
