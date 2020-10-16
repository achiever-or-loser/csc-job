package com.csc.job.core.biz.model;

import java.io.Serializable;

/**
 * @Description:
 * @PackageName: com.csc.job.core.biz.model
 * @Author: 陈世超
 * @Create: 2020-10-14 15:56
 * @Version: 1.0
 */
public class LogParam implements Serializable {
    private static final long serialVersionUID = 42L;

    public LogParam() {
    }

    public LogParam(long logId, long logDateTim, int fromLineNum) {
        this.logId = logId;
        this.logDateTim = logDateTim;
        this.fromLineNum = fromLineNum;
    }

    private long logId;
    private long logDateTim;
    private int fromLineNum;

    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public long getLogDateTim() {
        return logDateTim;
    }

    public void setLogDateTim(long logDateTim) {
        this.logDateTim = logDateTim;
    }

    public int getFromLineNum() {
        return fromLineNum;
    }

    public void setFromLineNum(int fromLineNum) {
        this.fromLineNum = fromLineNum;
    }
}
