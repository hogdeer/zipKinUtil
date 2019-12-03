package com.hogdeer.zipkin.filter.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.alibaba.fastjson.JSONObject;
import com.github.kristofa.brave.*;
import com.github.kristofa.brave.internal.Nullable;
import com.twitter.zipkin.gen.Span;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Activate(group = Constants.CONSUMER)
public class DrpcClientInterceptor implements Filter{


    private final static Logger logger = LoggerFactory.getLogger(DrpcClientInterceptor.class);

    private static Brave brave;
    private static ClientRequestInterceptor clientRequestInterceptor;
    private static ClientResponseInterceptor clientResponseInterceptor;
    private static ClientSpanThreadBinder clientSpanThreadBinder;


    public static void setBrave(Brave brave) {
        DrpcClientInterceptor.brave = brave;
        DrpcClientInterceptor.clientRequestInterceptor = brave.clientRequestInterceptor();
        DrpcClientInterceptor.clientResponseInterceptor = brave.clientResponseInterceptor();
        DrpcClientInterceptor.clientSpanThreadBinder=brave.clientSpanThreadBinder();
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        clientRequestInterceptor.handle(new GrpcClientRequestAdapter(invocation));
//        Map<String, String> att = invocation.getAttachments();
        final Span currentClientSpan = clientSpanThreadBinder.getCurrentClientSpan();
        Result result;
        try {
            result = invoker.invoke(invocation);
            clientSpanThreadBinder.setCurrentSpan(currentClientSpan);
            clientResponseInterceptor.handle(new GrpcClientResponseAdapter(result));


       /*     Result rpcResult = invoker.invoke(invocation);
            clientResponseInterceptor.handle(new DubboClientResponseAdapter(rpcResult));*/


        } finally {
          //  clientSpanThreadBinder.setCurrentSpan(null);
        }
        return result;
    }


    static final class GrpcClientRequestAdapter implements ClientRequestAdapter {
        private Invocation invocation;

        public GrpcClientRequestAdapter(Invocation invocation) {
            this.invocation = invocation;
        }

        @Override
        public String getSpanName() {
            String ls = invocation.getMethodName();
            String serviceName = ls == null ? "unkown" : ls;
//            return serviceName;

            return  RpcContext.getContext().getUrl().getPath()+"#"+serviceName;
        }

        @Override
        public void addSpanIdToRequest(@Nullable SpanId spanId) {
            Map<String, String> at = this.invocation.getAttachments();
            if (spanId == null) {
                at.put("Sampled", "0");
            } else {
                at.put("Sampled", "1");
                at.put("TraceId", spanId.traceIdString());
                at.put("SpanId", IdConversion.convertToString(spanId.spanId));
                if (spanId.nullableParentId() != null) {
                    at.put("ParentSpanId", IdConversion.convertToString(spanId.parentId));
                }
            }
        }


        @Override
        public Collection<KeyValueAnnotation> requestAnnotations() {

            Map<String, String> dubboParameters = RpcContext.getContext().getUrl().getParameters();
            Object[] arguments = invocation.getArguments();
            URL url = invocation.getInvoker().getUrl();
            String methodName = invocation.getMethodName();
           // KeyValueAnnotation dubboUrl = KeyValueAnnotation.create("dubbo.url", url.toString());
           // KeyValueAnnotation dubboParm = KeyValueAnnotation.create("client.dubbo.params", JSONObject.toJSONString(dubboParameters));
            KeyValueAnnotation an = KeyValueAnnotation.create("params", JSONObject.toJSONString(arguments));
            KeyValueAnnotation interfaceName = KeyValueAnnotation.create("interface", url.getPath()+"#"+methodName);
            KeyValueAnnotation addrAnnotation = KeyValueAnnotation.create("address", RpcContext.getContext().getRemoteAddress().toString());
            KeyValueAnnotation projectAnnotation = KeyValueAnnotation.create("zipkinProjectType", "ds-client");

            List<KeyValueAnnotation> kvs = new ArrayList<KeyValueAnnotation>();
           // kvs.add(dubboUrl);
            kvs.add(an);
           // kvs.add(dubboParm);
            kvs.add(interfaceName);
            kvs.add(addrAnnotation);
            kvs.add(projectAnnotation);

            return kvs;
        }

        @Override
        public com.twitter.zipkin.gen.Endpoint serverAddress() {
            return null;
        }
    }

    static final class GrpcClientResponseAdapter implements ClientResponseAdapter {

        private final Result result;

        public GrpcClientResponseAdapter(Result result) {
            this.result = result;
        }

        @Override
        public Collection<KeyValueAnnotation> responseAnnotations() {
//            return !result.hasException()
//                ? Collections.<KeyValueAnnotation>emptyList()
//                : Collections.singletonList(KeyValueAnnotation.create(zipkin.Constants.ERROR, result.getException().getMessage()));

            return !result.hasException()
                    ? Collections.<KeyValueAnnotation>emptyList()
                    : Collections.singletonList(KeyValueAnnotation.create(zipkin.Constants.ERROR, ExceptionUtils.getStackTrace(result.getException())));

        }
    }
}
