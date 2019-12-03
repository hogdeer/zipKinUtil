package com.hogdeer.zipkin.mybaits;

import com.alibaba.fastjson.JSON;
import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.ClientTracer;
import com.twitter.zipkin.gen.Endpoint;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import zipkin.Constants;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.util.Properties;



@Intercepts({@Signature(
        type = Executor.class,
        method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}
),@Signature(
        type = Executor.class,
        method = "query",
        args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}
), @Signature(
        type = Executor.class,
        method = "update",
        args = {MappedStatement.class, Object.class}
)})

/***
 *
 * Mybaits 拦截器
 *
 * 拦截sql  参数，
 *
 * 拦截 sql 返回的结果；
 *
 *
 */
public class MybaitsZipkinInterceptor  implements Interceptor  {


    private final static Logger logger = LoggerFactory.getLogger(MybaitsZipkinInterceptor.class);


    private static Brave brave;
    private static ClientTracer clientTracer;



    public  void setBrave(Brave brave) {
        MybaitsZipkinInterceptor.brave = brave;
        MybaitsZipkinInterceptor.clientTracer=brave.clientTracer();
    }




    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        ClientTracer tracer=clientTracer;
        Object result=null;
        if (tracer!=null){
            Exception exception=null;
            Object[] args = invocation.getArgs();
            MappedStatement ms = (MappedStatement)args[0];
            Object parameter = args[1];
            Executor executor = (Executor)invocation.getTarget();
            Connection connection = executor.getTransaction().getConnection();
            String msId= ms.getId();
            BoundSql boundSql = ms.getBoundSql(parameter);
            Object object= boundSql.getParameterObject();
            String sql = boundSql.getSql();
            String methodName = invocation.getMethod().getName();
            try {
                this.beginTrace(clientTracer,connection, methodName,sql,object,msId);
            }catch (Exception e){
                logger.error("链路信息采集失败，",e);
            }


            try{
                result = invocation.proceed();
            }catch (Exception e){
                exception=e;
            }finally {
                this.endTrace(clientTracer,exception,result);
            }
        }else{
            result = invocation.proceed();
        }

        return result;
    }




    @Override
    public Object plugin(Object o) {
        return Plugin.wrap(o, this);
    }

    @Override
    public void setProperties(Properties properties) {

    }






    private void beginTrace(ClientTracer tracer, Connection connection,String newSpan,String sql,Object parames,String msId) {
        tracer.startNewSpan(msId);
        tracer.submitBinaryAnnotation("sql.query", sql);
        tracer.submitBinaryAnnotation("mapper.path", msId);
        if(null != parames){
            String className = parames.getClass().getSimpleName();
            tracer.submitBinaryAnnotation("parames.className", className);
            tracer.submitBinaryAnnotation("sql.parames", JSON.toJSONString(parames));
        }
        tracer.submitBinaryAnnotation("sql.query", sql);
        try {
            //自定义 输出信息和Services
            this.setClientSent(tracer, connection);
        } catch (Exception e) {
            //默认在同一个Services
            tracer.setClientSent();
        }
    }


    private void setClientSent(ClientTracer tracer, Connection connection) throws Exception {
        URI url = URI.create(connection.getMetaData().getURL().substring(5));
        InetAddress address = Inet4Address.getByName(url.getHost());
        int ipv4 = ByteBuffer.wrap(address.getAddress()).getInt();
        int port = url.getPort() == -1 ? 3306 : url.getPort();
        String serviceName = connection.getMetaData().getDatabaseProductName().toLowerCase();
        String databaseName = connection.getCatalog();
        if (databaseName != null && !"".equals(databaseName)) {
            serviceName = serviceName + "-" + databaseName;
        }
        tracer.setClientSent(Endpoint.builder().ipv4(ipv4).port(port).serviceName(serviceName).build());
    }




    private void endTrace(ClientTracer tracer, Exception exception, Object result) {
        try {
            if (exception != null) {
                String errMsg = ExceptionUtils.getStackTrace(exception);
                tracer.submitBinaryAnnotation(Constants.ERROR, "1");
                tracer.submitBinaryAnnotation("error.msg", errMsg);
            }
            if(null != result){
                tracer.submitBinaryAnnotation("sql.result", JSON.toJSONString(result));
            }
        } finally {
            tracer.setClientReceived();
        }

    }

}
