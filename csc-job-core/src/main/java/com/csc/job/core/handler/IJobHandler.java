package com.csc.job.core.handler;

import com.csc.job.core.biz.model.ReturnT;

import java.lang.reflect.InvocationTargetException;

/**
 * @Description:
 * @PackageName: com.csc.job.core.handler
 * @Author: 陈世超
 * @Create: 2020-10-14 18:05
 * @Version: 1.0
 */
public abstract class IJobHandler {
    public static final ReturnT<String> SUCCESS = new ReturnT<>(200, null);
    public static final ReturnT<String> FAIL = new ReturnT<>(500, null);
    public static final ReturnT<String> FAIL_TIMEOUT = new ReturnT<>(502, null);

    public abstract ReturnT<String> execute(String param) throws Exception;

    public void init() throws InvocationTargetException, IllegalAccessException {}

    public void destroy() throws InvocationTargetException, IllegalAccessException {}
}
