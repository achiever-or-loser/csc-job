package com.csc.job.core.glue.impl;

import com.csc.job.core.executor.impl.CscJobSpringExecutor;
import com.csc.job.core.glue.GlueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.AnnotationUtils;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @Description:
 * @PackageName: com.csc.job.core.glue
 * @Author: 陈世超
 * @Create: 2020-10-15 17:17
 * @Version: 1.0
 */
public class SpringGlueFactory extends GlueFactory {
    private static Logger logger = LoggerFactory.getLogger(SpringGlueFactory.class);

    @Override
    public void injectService(Object instance) {
        if (instance == null) return;
        if (CscJobSpringExecutor.getApplicationContext() == null) return;
        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) continue;
            Object fieldBean = null;
            if (AnnotationUtils.getAnnotation(field, Resource.class) != null) {
                Resource resource = AnnotationUtils.getAnnotation(field, Resource.class);
                if (resource.name() != null && resource.name().trim().length() > 0) {
                    fieldBean = CscJobSpringExecutor.getApplicationContext().getBean(resource.name());
                } else {
                    fieldBean = CscJobSpringExecutor.getApplicationContext().getBean(field.getName());
                }
                if (fieldBean == null)
                    fieldBean = CscJobSpringExecutor.getApplicationContext().getBean(field.getType());
            } else if (AnnotationUtils.getAnnotation(field, Autowired.class) != null) {
                Qualifier qualifier = AnnotationUtils.getAnnotation(field, Qualifier.class);
                if (qualifier != null && qualifier.value() != null && qualifier.value().length() > 0) {
                    fieldBean = CscJobSpringExecutor.getApplicationContext().getBean(qualifier.value());
                } else {
                    fieldBean = CscJobSpringExecutor.getApplicationContext().getBean(field.getType());
                }
            }
            if (fieldBean != null) {
                field.setAccessible(true);
                try {
                    field.set(instance, fieldBean);
                } catch (IllegalAccessException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }
}
