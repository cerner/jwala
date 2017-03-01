package com.cerner.jwala.web.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * A filter that redirects legacy REST service url calls e.g. jwala/v1.0 to jwala/services/v1.0
 *
 * Created by Jedd Cuison on 8/8/2016.
 */
@WebFilter(urlPatterns = "/v1.0/*")
public class RestWebServiceLegacyUrlRedirect implements Filter {

    private static final String VERSION_1 = "/v1.0";
    private static final String SERVICES = "/services";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException {

        final HttpServletRequest httpReq = (HttpServletRequest) request;
        if (httpReq.getRequestURI().startsWith(httpReq.getContextPath() + VERSION_1)) {
            final String forwardPath = httpReq.getRequestURI().replace(httpReq.getContextPath() + VERSION_1,
                    SERVICES + VERSION_1);
            request.getRequestDispatcher(forwardPath).forward(request, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }
}