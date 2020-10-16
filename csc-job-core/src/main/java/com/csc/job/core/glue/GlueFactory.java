package com.csc.job.core.glue;

import com.csc.job.core.glue.impl.SpringGlueFactory;
import com.csc.job.core.handler.IJobHandler;
import groovy.lang.GroovyClassLoader;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description:
 * @PackageName: com.csc.job.core.glue
 * @Author: 陈世超
 * @Create: 2020-10-15 17:15
 * @Version: 1.0
 */
public class GlueFactory {
    private static GlueFactory glueFactory = new GlueFactory();

    public static GlueFactory getInstance() {
        return glueFactory;
    }

    public void refresh(int type) {
        if (type == 0) {
            glueFactory = new GlueFactory();
        } else if (type == 1) {
            glueFactory = new SpringGlueFactory();
        }
    }

    private GroovyClassLoader groovyClassLoader = new GroovyClassLoader();
    private ConcurrentHashMap<String, Class<?>> CLASS_CACHE = new ConcurrentHashMap<>();

    public IJobHandler loadNewInstance(String codeSource) throws IllegalAccessException, InstantiationException {
        if (codeSource != null && codeSource.trim().length() > 0) {
            Class<?> clazz = getCodeSourceClass(codeSource);
            if (clazz != null) {
                Object instance = clazz.newInstance();
                if (instance != null) {
                    if (instance instanceof IJobHandler) {
                        this.injectService(instance);
                        return (IJobHandler) instance;
                    } else {
                        throw new IllegalArgumentException(">>>>>>>>>>> csc-glue, loadNewInstance error, "
                                + "cannot convert from instance[" + instance.getClass() + "] to IJobHandler");
                    }
                }
            }
        }
        throw new IllegalArgumentException(">>>>>>>>>>> csc-glue, loadNewInstance error, instance is null");
    }

    public void injectService(Object instance) {
    }

    private Class<?> getCodeSourceClass(String codeClass) {
        try {
            byte[] md5 = MessageDigest.getInstance("MD5").digest(codeClass.getBytes());
            String md5Str = new BigInteger(1, md5).toString(16);
            Class<?> clazz = CLASS_CACHE.get(md5Str);
            if (clazz == null) {
                clazz = groovyClassLoader.parseClass(codeClass);
                CLASS_CACHE.putIfAbsent(md5Str, clazz);
            }
            return clazz;
        } catch (NoSuchAlgorithmException e) {
            return groovyClassLoader.parseClass(codeClass);
        }
    }
}
