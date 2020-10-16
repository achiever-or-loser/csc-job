package com.csc.job.core.biz.client;

import com.csc.job.core.biz.AdminBiz;
import com.csc.job.core.biz.model.HandleCallbackParam;
import com.csc.job.core.biz.model.RegistryParam;
import com.csc.job.core.biz.model.ReturnT;
import com.csc.job.core.util.CscJobRemotingUtil;

import java.util.List;

/**
 * @Description:
 * @PackageName: com.csc.job.core.biz.client
 * @Author: 陈世超
 * @Create: 2020-10-13 13:27
 * @Version: 1.0
 */
public class AdminBizClient implements AdminBiz {
    public AdminBizClient(){}

    public AdminBizClient(String addressUrl, String accessToken) {
        this.addressUrl = addressUrl;
        this.accessToken = accessToken;
    }

    private String addressUrl;
    private String accessToken;
    private int timeout = 3;

    @Override
    public ReturnT<String> callback(List<HandleCallbackParam> callbackParamList) {
        return CscJobRemotingUtil.postBody(addressUrl+"api/callback",accessToken,timeout,callbackParamList,String.class);
    }

    @Override
    public ReturnT<String> registry(RegistryParam registryParam) {
        return CscJobRemotingUtil.postBody(addressUrl+"api/registry",accessToken,timeout,registryParam,String.class);
    }

    @Override
    public ReturnT<String> registryRemove(RegistryParam registryParam) {
        return CscJobRemotingUtil.postBody(addressUrl+"api/registryRemove",accessToken,timeout,registryParam,String.class);
    }
}
