package com.csc.job.core.biz.model;

import java.io.Serializable;

/**
 * @Description:
 * @PackageName: com.csc.job.core.biz.model
 * @Author: 陈世超
 * @Create: 2020-10-13 11:26
 * @Version: 1.0
 */
public class HandleCallbackParam implements Serializable {
    private static final long serialVersionUID = 42L;

    private Long logId;
    private Long logDateTim;
    private ReturnT<String> executeResult;

    public HandleCallbackParam() {
    }

    public HandleCallbackParam(Long logId, Long logDateTim, ReturnT<String> executeResult) {
        this.logId = logId;
        this.logDateTim = logDateTim;
        this.executeResult = executeResult;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public Long getLogDateTim() {
        return logDateTim;
    }

    public void setLogDateTim(Long logDateTim) {
        this.logDateTim = logDateTim;
    }

    public ReturnT<String> getExecuteResult() {
        return executeResult;
    }

    public void setExecuteResult(ReturnT<String> executeResult) {
        this.executeResult = executeResult;
    }

    @Override
    public String toString() {
        return "HandleCallbackParam{" +
                "logId=" + logId +
                ", logDateTim=" + logDateTim +
                ", executeResult=" + executeResult +
                '}';
    }
}
