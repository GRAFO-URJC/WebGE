package com.gramevapp.config;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Collection;

/**
 * https://www.baeldung.com/spring_redirect_after_login
 */
public class UrlAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    protected Log logger = LogFactory.getLog(this.getClass());

    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Authentication authentication)
            throws IOException {
        handle(httpServletRequest, httpServletResponse, httpServletRequest.getSession(), authentication);
        clearAuthenticationAttributes(httpServletRequest);
    }

    protected void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            HttpSession session,
            Authentication authentication
    ) throws IOException {

        String targetUrl = determineTargetUrl(authentication, session);

        if (response.isCommitted()) {
            logger.debug(
                    "Response has already been committed. Unable to redirect to "
                            + targetUrl);
            return;
        }

        redirectStrategy.sendRedirect(request, response, targetUrl);
    }

    protected String determineTargetUrl(final Authentication authentication, HttpSession session) {
        final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String savedContext = session.getAttribute("SPRING_SECURITY_SAVED_REQUEST") == null ? null :
                ((DefaultSavedRequest) session.getAttribute("SPRING_SECURITY_SAVED_REQUEST")).getRequestURI();
        for (final GrantedAuthority grantedAuthority : authorities) {
            String authorityName = grantedAuthority.getAuthority();
            if (authorityName.equals("ROLE_USER")) {
                return savedContext != null ? savedContext : "/user";
            } else if (authorityName.equals("ROLE_ADMIN")) {
                return savedContext != null ? savedContext : "/admin";
            }
        }

        throw new IllegalStateException();
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }
}
