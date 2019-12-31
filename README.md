# zipKinUtil
Spring mvc + dubbo+ mysql  链路跟踪

是基于spring 3.2.8 dubbo2.8.4 版本 通过拦截器实现链路跟踪；
1.浏览器 --- rest ---dubbo 服务---mysql ；

效果图:  
![效果图](https://raw.githubusercontent.com/hogdeer/zipKinUtil/develop/img/1.png)  
![效果图](https://raw.githubusercontent.com/hogdeer/zipKinUtil/develop/img/2.png)  
![效果图](https://raw.githubusercontent.com/hogdeer/zipKinUtil/develop/img/3.png)  



ds spring配置文件添加配置


	<bean id="brave" class="com.hogdeer.zipkin.brave.BraveFactoryBean">
		<property name="zipkinHost" value="http://127.0.0.1:9411"/>
		<property name="serviceName" value="membership-ds"/>
	</bean>


mybatis 插件配置添加

<bean class="com.hogdeer.zipkin.mybaits.MybaitsZipkinInterceptor">
	<property name="brave" ref="brave"/>
</bean>



  
  rest  spring配置文件添加配置
  
  
  <bean id="brave" class="com.hogdeer.zipkin.brave.BraveFactoryBean">
        <property name="zipkinHost" value="http://127.0.0.1:9411"/>
        <property name="serviceName" value="boss-rest"/>
    </bean>



    <mvc:interceptors>
        <bean  class="com.hogdeer.zipkin.interceptor.CustomServletHandlerInterceptor" >
            <constructor-arg value="#{brave}"/>
        </bean>
    </mvc:interceptors>

  
  
  
	</bean>
