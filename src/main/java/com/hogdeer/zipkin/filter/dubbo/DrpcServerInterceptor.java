package com.hogdeer.zipkin.filter.dubbo;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.alibaba.fastjson.JSON;
import com.github.kristofa.brave.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.*;

import static com.github.kristofa.brave.IdConversion.convertToLong;


@Activate(group = Constants.PROVIDER )
public class DrpcServerInterceptor implements Filter {

    private final static Logger logger = LoggerFactory.getLogger(DrpcServerInterceptor.class);

    private static  Brave brave;
    private static  ServerRequestInterceptor serverRequestInterceptor;
    private static  ServerResponseInterceptor serverResponseInterceptor;





    public static void setBrave(Brave brave) {
        DrpcServerInterceptor.brave = brave;
        DrpcServerInterceptor.serverRequestInterceptor = brave.serverRequestInterceptor();
        DrpcServerInterceptor.serverResponseInterceptor = brave.serverResponseInterceptor();
    }


    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        serverRequestInterceptor.handle(new DrpcServerRequestAdapter(invocation));
        Result result ;
        try {
            result =  invoker.invoke(invocation);
            serverResponseInterceptor.handle(new GrpcServerResponseAdapter(result));
        } finally {

        }
        return result;
    }

    static final class DrpcServerRequestAdapter implements ServerRequestAdapter {
        private Invocation invocation;
        DrpcServerRequestAdapter(Invocation invocation) {
            this.invocation = invocation;
        }

        @Override
        public TraceData getTraceData() {
            //Random randoml = new Random();
            Map<String, String> at = this.invocation.getAttachments();
            String sampled = at.get("Sampled");
            String parentSpanId = at.get("ParentSpanId");
            String traceId = at.get("TraceId");
            String spanId = at.get("SpanId");

            // Official sampled value is 1, though some old instrumentation send true
            Boolean parsedSampled = sampled != null
                    ? sampled.equals("1") || sampled.equalsIgnoreCase("true")
                    : null;

            if (traceId != null && spanId != null) {
                return TraceData.create(getSpanId(traceId, spanId, parentSpanId, parsedSampled));
            } else if (parsedSampled == null) {
                return TraceData.EMPTY;
            } else if (parsedSampled.booleanValue()) {
                // Invalid: The caller requests the trace to be sampled, but didn't pass IDs
                return TraceData.EMPTY;
            } else {
                return TraceData.NOT_SAMPLED;
            }
        }

        @Override
        public String getSpanName() {
            String ls = invocation.getMethodName();
            String serviceName = ls == null?"unkown":ls;
            return  RpcContext.getContext().getUrl().getPath()+"#"+serviceName;
        }

        @Override
        public Collection<KeyValueAnnotation> requestAnnotations() {

            Map<String, String> dubboParameters = RpcContext.getContext().getUrl().getParameters();
         //   KeyValueAnnotation dubboParm = KeyValueAnnotation.create("service.dubbo.params", JSONObject.toJSONString(dubboParameters));
            SocketAddress socketAddress = RpcContext.getContext().getRemoteAddress();
            KeyValueAnnotation projectAnnotation = KeyValueAnnotation.create("zipkinProjectType", "ds-service");
            List<KeyValueAnnotation> kvs = new ArrayList<KeyValueAnnotation>();
            kvs.add(projectAnnotation);
          //  kvs.add(dubboParm);
            if (socketAddress != null) {
                KeyValueAnnotation remoteAddrAnnotation = KeyValueAnnotation.create(
                        "DRPC_REMOTE_ADDR", socketAddress.toString());
                kvs.add(remoteAddrAnnotation);
            }
            return  kvs;
        }
    }

    static final class GrpcServerResponseAdapter implements ServerResponseAdapter {

        final Result result;

        public GrpcServerResponseAdapter(Result result) {
            this.result = result;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Collection<KeyValueAnnotation> responseAnnotations() {
            if (!result.hasException()){
                return Collections.singletonList(KeyValueAnnotation.create("result", JSON.toJSONString(result)));
            }else{
//               return Collections.singletonList(KeyValueAnnotation.create(zipkin.Constants.ERROR, result.getException().getMessage()));
                return Collections.singletonList(KeyValueAnnotation.create(zipkin.Constants.ERROR, ExceptionUtils.getStackTrace(result.getException())));
            }
        }

    }

    static SpanId getSpanId(String traceId, String spanId, String parentSpanId, Boolean sampled) {
        return SpanId.builder()
                .traceIdHigh(traceId.length() == 32 ? convertToLong(traceId, 0) : 0)
                .traceId(convertToLong(traceId))
                .spanId(convertToLong(spanId))
                .sampled(sampled)
                .parentId(parentSpanId == null ? null : convertToLong(parentSpanId)).build();
    }
}
