package dev.xuya.common.web.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** XSS 防护过滤器 */
public class XssFilter implements Filter {

    private final List<String> excludePaths = new ArrayList<>();
    private final AntPathMatcher matcher = new AntPathMatcher();

    public XssFilter(String... excludes) {
        excludePaths.addAll(List.of(excludes));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        for (String pattern : excludePaths) {
            if (matcher.match(pattern, req.getServletPath())) {
                chain.doFilter(request, response);
                return;
            }
        }
        chain.doFilter(new XssHttpServletRequestWrapper(req), response);
    }

    static class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {
        XssHttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getParameter(String name) {
            return clean(super.getParameter(name));
        }

        @Override
        public String[] getParameterValues(String name) {
            String[] values = super.getParameterValues(name);
            if (values == null) return null;
            String[] cleaned = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                cleaned[i] = clean(values[i]);
            }
            return cleaned;
        }

        private String clean(String value) {
            return value == null ? null : value.replaceAll("<[^>]*>", "").trim();
        }
    }
}
