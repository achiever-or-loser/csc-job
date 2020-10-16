package com.csc.job.core.executor.impl;

import com.csc.job.core.biz.model.ReturnT;
import com.csc.job.core.executor.CscJobExecutor;
import com.csc.job.core.glue.GlueFactory;
import com.csc.job.core.handler.annotation.CscJob;
import com.csc.job.core.handler.impl.MethodJobHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.AnnotatedArrayType;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @Description:
 * @PackageName: com.csc.job.core.executor.impl
 * @Author: 陈世超
 * @Create: 2020-10-16 13:39
 * @Version: 1.0
 */
public class CscJobSpringExecutor extends CscJobExecutor implements ApplicationContextAware, SmartInitializingSingleton, DisposableBean {
    private static Logger logger = LoggerFactory.getLogger(CscJobSpringExecutor.class);

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void afterSingletonsInstantiated() {
        initJobHandlerMethodRepository(applicationContext);
        GlueFactory.getInstance().refresh(1);
        super.start();
    }

    private void initJobHandlerMethodRepository(ApplicationContext applicationContext) {
        if (applicationContext == null) return;
        String[] beanDefinitionNames = applicationContext.getBeanNamesForType(Object.class, false, true);
        for (String beanDefinitionName : beanDefinitionNames) {
            Object bean = applicationContext.getBean(beanDefinitionName);
            Map<Method, CscJob> annotatedMethod = null;

            annotatedMethod = MethodIntrospector.selectMethods(bean.getClass(), new MethodIntrospector.MetadataLookup<CscJob>() {
                @Override
                public CscJob inspect(Method method) {
                    return AnnotatedElementUtils.findMergedAnnotation(method, CscJob.class);
                }
            });
            if (annotatedMethod == null || annotatedMethod.isEmpty()) continue;
            for (Map.Entry<Method, CscJob> methodCscJobEntry : annotatedMethod.entrySet()) {
                Method method = methodCscJobEntry.getKey();
                CscJob cscJob = methodCscJobEntry.getValue();
                if (cscJob == null) continue;
                String name = cscJob.value();
                if (name.trim().length() == 0)
                    throw new RuntimeException("csc-job method-jobhandler name invalid, for[" + bean.getClass() + "#" + method.getName() + "] .");
                if (loadJobHandler(name) != null)
                    throw new RuntimeException("csc-job jobhandler[" + name + "] naming conflicts.");
                if (!(method.getParameterTypes().length == 1 && method.getParameterTypes()[0].isAssignableFrom(String.class))) {
                    throw new RuntimeException("csc-job method-jobhandler param-classtype invalid, for[" + bean.getClass() + "#" + method.getName() + "] , " +
                            "The correct method format like \" public ReturnT<String> execute(String param) \" .");
                }
                if (!method.getReturnType().isAssignableFrom(ReturnT.class)) {
                    throw new RuntimeException("csc-job method-jobhandler return-classtype invalid, for[" + bean.getClass() + "#" + method.getName() + "] , " +
                            "The correct method format like \" public ReturnT<String> execute(String param) \" .");
                }
                method.setAccessible(true);
                Method initMethod = null;
                Method destroyMethod = null;

                if (cscJob.init().trim().length() > 0) {
                    try {
                        initMethod = bean.getClass().getDeclaredMethod(cscJob.init());
                        initMethod.setAccessible(true);
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }
                if (cscJob.destroy().trim().length() > 0) {
                    try {
                        destroyMethod = bean.getClass().getDeclaredMethod(cscJob.destroy());
                        destroyMethod.setAccessible(true);
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }
                }
                registJobHandler(name, new MethodJobHandler(bean, method, initMethod, destroyMethod));
            }
        }
    }

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}

