package com.hogdeer.zipkin.interceptor.adapter;

import com.github.kristofa.brave.KeyValueAnnotation;
import com.github.kristofa.brave.ServerRequestAdapter;
import com.github.kristofa.brave.SpanId;
import com.github.kristofa.brave.TraceData;
import com.github.kristofa.brave.http.BraveHttpHeaders;
import com.github.kristofa.brave.http.HttpServerRequest;
import com.github.kristofa.brave.http.SpanNameProvider;
import com.github.kristofa.brave.servlet.ServletHttpServerRequest;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import static com.github.kristofa.brave.IdConversion.convertToLong;
//import zipkin.TraceKeys;


public class CustomServerRequestAdapter implements ServerRequestAdapter {

    private final HttpServletRequest request;

    private final SpanNameProvider spanNameProvider;
    private final String data;
//    private final BraveUserInfo braveUserInfo;

    private final HttpServerRequest serviceRequest;
    private final String url;

    private static String localIp;
    private static Integer localPort;

//    public CustomServerRequestAdapter(HttpServletRequest request, SpanNameProvider spanNameProvider) {
//        this.request = request;
//    }



    public CustomServerRequestAdapter(HttpServletRequest request, String data, SpanNameProvider spanNameProvider) {
        this.request = request;
        this.spanNameProvider = spanNameProvider;
        this.data = data;
        //this.braveUserInfo = braveUserInfo;
        this.serviceRequest = new ServletHttpServerRequest(request);
        this.url = request.getRequestURI();
    }


    @Override
    public TraceData getTraceData() {
        String sampled = request.getHeader(BraveHttpHeaders.Sampled.getName());
        String parentSpanId = request.getHeader(BraveHttpHeaders.ParentSpanId.getName());
        String traceId = request.getHeader(BraveHttpHeaders.TraceId.getName());
        String spanId = request.getHeader(BraveHttpHeaders.SpanId.getName());

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
//        return "custom spanName";
        return connectMulString("", this.url, "(", spanNameProvider.spanName(serviceRequest), ")");
    }

    @Override
    public Collection<KeyValueAnnotation> requestAnnotations() {
        if(null == localPort){
            localPort = this.request.getLocalPort();
        }
        if(StringUtils.isBlank(localIp)){
            localIp = getLocalIp();
        }

        KeyValueAnnotation uriAnnotation = KeyValueAnnotation.create(zipkin.TraceKeys.HTTP_URL, serviceRequest.getUri().toString());
        KeyValueAnnotation addrAnnotation = KeyValueAnnotation.create("address", connectMulString(":", localIp, localPort));
        KeyValueAnnotation dataAnnotation = KeyValueAnnotation.create("data", data);
        KeyValueAnnotation projectAnnotation = KeyValueAnnotation.create("zipkinProjectType", "web");
        List<KeyValueAnnotation> kvs = new ArrayList<KeyValueAnnotation>();
        kvs.add(uriAnnotation);
        kvs.add(addrAnnotation);
        kvs.add(dataAnnotation);
        kvs.add(projectAnnotation);

        return kvs;
    }

    static SpanId getSpanId(String traceId, String spanId, String parentSpanId, Boolean sampled) {
        return SpanId.builder()
                .traceIdHigh(traceId.length() == 32 ? convertToLong(traceId, 0) : 0)
                .traceId(convertToLong(traceId))
                .spanId(convertToLong(spanId))
                .sampled(sampled)
                .parentId(parentSpanId == null ? null : convertToLong(parentSpanId)).build();
    }




    public static String connectMulString(String joinStr, Object... key){
        if(null == joinStr){
            joinStr = "_";
        }
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for(Object item:key){
            if(null != item){
                if(count > 0){
                    sb.append(joinStr);
                }
                sb.append(item);
                count++;
            }
        }
        return sb.toString();
    }




    public static String getLocalIp()
    {
        String localip = null;// 本地IP，如果没有配置外网IP则返回它
        try
        {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            InetAddress ip = null;
            boolean finded = false;// 是否找到外网IP
            while (netInterfaces.hasMoreElements() && !finded)
            {
                NetworkInterface ni = netInterfaces.nextElement();
                Enumeration<InetAddress> address = ni.getInetAddresses();
                while (address.hasMoreElements())
                {
                    ip = address.nextElement();
                    if (ip.isSiteLocalAddress() && !ip.isLoopbackAddress()
                            && ip.getHostAddress().indexOf(":") == -1)
                    {
                        localip = ip.getHostAddress();
                        finded = true;
                        break;
                    }
                }
            }
        }
        catch (SocketException e)
        {
           // logger.error(e.getMessage(), e);
        }
        return localip;
    }
}
