package com.csc.job.core.biz.client;

import com.csc.job.core.biz.ExecutorBiz;
import com.csc.job.core.biz.model.*;
import com.csc.job.core.util.CscJobRemotingUtil;

/**
 * @Description:
 * @PackageName: com.csc.job.core.biz.client
 * @Author: 陈世超
 * @Create: 2020-10-16 13:35
 * @Version: 1.0
 */
public class ExecutorBizClient implements ExecutorBiz {
    public ExecutorBizClient() {
    }

    public ExecutorBizClient(String addressUrl, String accessToken) {
        this.addressUrl = addressUrl;
        this.accessToken = accessToken;
        if (addressUrl.endsWith("/"))
            this.addressUrl += "/";
    }

    private String addressUrl;
    private String accessToken;
    private int timeout = 3;


    @Override
    public ReturnT<String> beat() {
        return CscJobRemotingUtil.postBody(addressUrl + "beat", accessToken, timeout, null, String.class);
    }

    @Override
    public ReturnT<String> idleBeat(IdleBeatParam idleBeatParam) {
        return CscJobRemotingUtil.postBody(addressUrl + "idleBeat", accessToken, timeout, idleBeatParam, String.class);
    }

    @Override
    public ReturnT<String> run(TriggerParam triggerParam) {
        return CscJobRemotingUtil.postBody(addressUrl + "run", accessToken, timeout, triggerParam, String.class);
    }

    @Override
    public ReturnT<String> kill(KillParam killParam) {
        return CscJobRemotingUtil.postBody(addressUrl + "kill", accessToken, timeout, killParam, String.class);
    }

    @Override
    public ReturnT<LogResult> log(LogParam logParam) {
        return CscJobRemotingUtil.postBody(addressUrl + "log", accessToken, timeout, logParam, String.class);
    }
}
