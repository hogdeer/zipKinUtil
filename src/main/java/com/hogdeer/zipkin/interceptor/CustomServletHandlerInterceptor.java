package com.hogdeer.zipkin.interceptor;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import com.github.kristofa.brave.*;
import com.github.kristofa.brave.http.DefaultSpanNameProvider;
import com.github.kristofa.brave.http.HttpResponse;
import com.github.kristofa.brave.http.HttpServerResponseAdapter;
import com.github.kristofa.brave.http.SpanNameProvider;
import com.github.kristofa.brave.spring.ServletHandlerInterceptor;
import com.hogdeer.zipkin.interceptor.adapter.CustomServerRequestAdapter;
import com.hogdeer.zipkin.constant.zipkinConstant;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class CustomServletHandlerInterceptor extends HandlerInterceptorAdapter {

    static final String HTTP_SERVER_SPAN_ATTRIBUTE = ServletHandlerInterceptor.class.getName() + ".customserver-span";


    private final ServerRequestInterceptor requestInterceptor;
    private final ServerResponseInterceptor responseInterceptor;
    private final ServerSpanThreadBinder serverThreadBinder;
    private final SpanNameProvider spanNameProvider;
    private final Brave brave;





    CustomServletHandlerInterceptor(Brave brave) {
        this.brave=brave;
        this.requestInterceptor = brave.serverRequestInterceptor();
        this.responseInterceptor = brave.serverResponseInterceptor();
        this.serverThreadBinder = brave.serverSpanThreadBinder();
        this.spanNameProvider = new DefaultSpanNameProvider();
    }



    @Override
    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) throws Exception {
        String data = this.getData(request);
        CustomServerRequestAdapter csRequest = new CustomServerRequestAdapter(request,data, spanNameProvider);
        requestInterceptor.handle(csRequest);
        Long traceId = brave.serverSpanThreadBinder().getCurrentServerSpan().getSpan().getTrace_id();
        response.setHeader("traceId", IdConversion.convertToString(traceId));
        return super.preHandle(request, response, handler);
    }

    @Override
    public void afterConcurrentHandlingStarted(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        request.setAttribute(HTTP_SERVER_SPAN_ATTRIBUTE, serverThreadBinder.getCurrentServerSpan());
        serverThreadBinder.setCurrentSpan(null);
    }

    @Override
    public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response, final Object handler, final Exception ex) {

        final ServerSpan span = (ServerSpan) request.getAttribute(HTTP_SERVER_SPAN_ATTRIBUTE);

        if (span != null) {
            serverThreadBinder.setCurrentSpan(span);
        }

        responseInterceptor.handle(new HttpServerResponseAdapter(new HttpResponse() {
            @Override
            public int getHttpStatusCode() {
                return response.getStatus();
            }
        }));
    }



    private String getData(final HttpServletRequest request){
        String data = StringUtils.EMPTY;
        JSONObject json = new JSONObject();
        Object obj = request.getAttribute(zipkinConstant.REQUEST_BODY_PARAM_JSON);

        if(!isObjectNull(obj)){
            json.put("requestBody", obj);
        }
        data = JSON.toJSONString(request.getParameterMap());
        if(StringUtils.isNotBlank(data)){
            json.put("form", data);
        }
        data = request.getQueryString();
        if(StringUtils.isNotBlank(data)){
            json.put("query", data);
        }

        return json.toJSONString();
    }

    public static boolean isObjectNull(Object checkObj)
    {
        if(null == checkObj){
            return Boolean.TRUE;
        }
        return StringUtils.isBlank(checkObj.toString());
    }

}
