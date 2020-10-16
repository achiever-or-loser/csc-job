package com.csc.job.core.biz.model;

import java.io.Serializable;

/**
 * @Description:
 * @PackageName: com.csc.job.core.biz.model
 * @Author: 陈世超
 * @Create: 2020-10-13 9:16
 * @Version: 1.0
 */
public class LogResult implements Serializable {
    private static final long serialVersionUID = 42L;

    private int fromLineNum;
    private int toLineNum;
    private String content;
    private boolean isEnd;

    public LogResult(){}

    public LogResult(int fromLineNum, int toLineNum, String content, boolean isEnd) {
        this.fromLineNum = fromLineNum;
        this.toLineNum = toLineNum;
        this.content = content;
        this.isEnd = isEnd;
    }

    public int getFromLineNum() {
        return fromLineNum;
    }

    public void setFromLineNum(int fromLineNum) {
        this.fromLineNum = fromLineNum;
    }

    public int getToLineNum() {
        return toLineNum;
    }

    public void setToLineNum(int toLineNum) {
        this.toLineNum = toLineNum;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean end) {
        isEnd = end;
    }
}
