package com.csc.job.core.biz;

import com.csc.job.core.biz.model.HandleCallbackParam;
import com.csc.job.core.biz.model.RegistryParam;
import com.csc.job.core.biz.model.ReturnT;

import java.util.List;

/**
 * @Description:
 * @PackageName: com.csc.job.core.biz
 * @Author: 陈世超
 * @Create: 2020-10-13 9:36
 * @Version: 1.0
 */
public interface AdminBiz {
    public ReturnT<String> callback(List<HandleCallbackParam> callbackParamList);

    public ReturnT<String> registry(RegistryParam registryParam);

    public ReturnT<String> registryRemove(RegistryParam registryParam);
}
