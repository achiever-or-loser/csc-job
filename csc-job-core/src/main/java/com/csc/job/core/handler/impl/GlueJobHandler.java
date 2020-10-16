package com.csc.job.core.handler.impl;

import com.csc.job.core.biz.model.ReturnT;
import com.csc.job.core.handler.IJobHandler;
import com.csc.job.core.log.CscJobLog;

/**
 * @Description:
 * @PackageName: com.csc.job.core.handler.impl
 * @Author: 陈世超
 * @Create: 2020-10-15 17:06
 * @Version: 1.0
 */
public class GlueJobHandler extends IJobHandler {
    private long glueUpdatetime;
    private IJobHandler jobHandler;

    public GlueJobHandler(long glueUpdatetime, IJobHandler jobHandler) {
        this.glueUpdatetime = glueUpdatetime;
        this.jobHandler = jobHandler;
    }

    public long getGlueUpdatetime() {
        return glueUpdatetime;
    }

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        CscJobLog.log("----------- glue.version:"+ glueUpdatetime +" -----------");
        return jobHandler.execute(param);
    }
}
