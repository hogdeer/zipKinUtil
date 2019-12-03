package com.hogdeer.zipkin.brave;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.EmptySpanCollectorMetricsHandler;
import com.github.kristofa.brave.LoggingSpanCollector;
import com.github.kristofa.brave.Sampler;
import com.github.kristofa.brave.http.HttpSpanCollector;
import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.FactoryBean;

import java.util.Map;
import java.util.logging.Logger;

/**
 * BraveBean管理器
 * <p>File：BraveFactoryBean.java</p>
 * <p>Title: </p>
 * <p>Description:</p>
 * @version 1.0
 */
public class BraveFactoryBean implements FactoryBean<Brave> { 
    private static final Logger LOGGER = Logger.getLogger(BraveFactoryBean.class.getName());
    /**服务名*/
    private String serviceName;
    /**zipkin服务器ip及端口，不配置默认打印日志*/ 
    private String zipkinHost;
    /**采样率 0~1 之间*/
    private float rate = 1.0f;
    /**单例模式*/
    private Brave instance;
    
//    private DiamondConfig braveConfig;
    /**发送类型：zipkin、kafka,默认为zipkin*/
    private String senderType;
    /**kafka服务器ip及端口*/
//    private String kafkaHost;

    private MysqlDataSource mysqlDataSource;

    public MysqlDataSource getMysqlDataSource() {
        return mysqlDataSource;
    }

    public void setMysqlDataSource(MysqlDataSource mysqlDataSource) {
        this.mysqlDataSource = mysqlDataSource;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getZipkinHost() {
        return zipkinHost;
    }

    public String getRate() {
        return String.valueOf(rate);
    }

    public void setRate(String rate) {
        this.rate = Float.parseFloat(rate);
    }

    public void setZipkinHost(String zipkinHost) {
        this.zipkinHost = zipkinHost;
    }

    public String getSenderType() {
		return senderType;
	}

	public void setSenderType(String senderType) {
		this.senderType = senderType;
	}

//	public DiamondConfig getBraveConfig() {
//		return braveConfig;
//	}
//
//	public void setBraveConfig(DiamondConfig braveConfig) {
//		this.braveConfig = braveConfig;
//	}

	private void createInstance() {
        if (this.serviceName == null) {
            throw new BeanInitializationException("Property serviceName must be set.");
        }
        Brave.Builder builder = new Brave.Builder(this.serviceName);
        if (this.zipkinHost != null && !"".equals(this.zipkinHost)) {
            builder.spanCollector(HttpSpanCollector.create(this.zipkinHost, new EmptySpanCollectorMetricsHandler()));
            builder.traceSampler(Sampler.create(rate)).build();
            LOGGER.info("brave dubbo config collect whith httpSpanColler , rate is "+ rate);
        }else{
            builder.spanCollector(new LoggingSpanCollector()).traceSampler(Sampler.create(rate)).build();
            LOGGER.info("brave dubbo config collect whith loggingSpanColletor , rate is "+ rate);
        }

        this.instance = builder.build();
    }

    @Override
    public Brave getObject() throws Exception {
        if (this.instance == null) {
            this.createInstance();
        }
        return this.instance;
    }


    @Override
    public Class<?> getObjectType() {
        return Brave.class;
    }

 
    @Override
    public boolean isSingleton() {
        return true;
    }
    
//    public void configInit(){
//    	Map<String, Object> configMap = this.braveConfig.getConfigMap();
//    	this.zipkinHost = (String) configMap.get("brave.zipkinHost");
//    	this.rate = Float.valueOf(configMap.get("brave.rate").toString());
//    	Object obj = configMap.get("brave.senderType");
//    	if(null != obj){
//    		this.senderType = obj.toString();
//    	}
//
//	}
}
