package com.csc.job.core.biz.model;

import java.io.Serializable;

/**
 * @Description:
 * @PackageName: com.csc.job.core.biz.model
 * @Author: 陈世超
 * @Create: 2020-10-14 15:54
 * @Version: 1.0
 */
public class KillParam implements Serializable {
    private static final long serialVersionUID = 42L;

    public KillParam() {
    }

    public KillParam(int jobId) {
        this.jobId = jobId;
    }

    private int jobId;

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }
}
