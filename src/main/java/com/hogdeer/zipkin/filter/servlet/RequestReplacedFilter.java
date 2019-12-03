package com.hogdeer.zipkin.filter.servlet;

import com.hogdeer.zipkin.constant.zipkinConstant;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter(filterName="requestReplacedFilter",urlPatterns={"/*"})
public class RequestReplacedFilter implements Filter {


    private final static Logger logger = LoggerFactory.getLogger(RequestReplacedFilter.class);


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest)request;
        String contentType = servletRequest.getContentType();
        if (StringUtils.isNotBlank(contentType) && contentType.startsWith("application/json")) {
            WrappedHttpServletRequest requestWrapper = new WrappedHttpServletRequest(servletRequest);
            String param = requestWrapper.getRequestParams();
            if (StringUtils.isNotBlank(param)) {
                requestWrapper.setAttribute(zipkinConstant.REQUEST_BODY_PARAM_JSON, param);
            }
            chain.doFilter(requestWrapper, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }
}
