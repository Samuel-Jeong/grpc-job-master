package com.dovaj.job_master_app_demo.filter;

import jakarta.servlet.*;

import java.io.IOException;

/**
 * packageName    : com.capshome.iotgw.common.filter
 * fileName       : NonLoggingMarkingFilter
 * author         : samuel
 * date           : 24. 8. 12.
 * description    : Tomcat access log 필터 클래스
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 24. 8. 12.        samuel       최초 생성
 */

public class NonLoggingMarkingFilter implements Filter {

    private String conditionUnlessKey;

    @Override
    public void init(FilterConfig config) {
        conditionUnlessKey = config.getInitParameter("conditionUnlessKey");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {
        request.setAttribute(conditionUnlessKey, conditionUnlessKey);
        chain.doFilter(request, response);
    }

}

