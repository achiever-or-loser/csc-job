package com.csc.job.core.handler.impl;

import com.csc.job.core.biz.model.ReturnT;
import com.csc.job.core.context.CscJobContext;
import com.csc.job.core.glue.GlueTypeEnum;
import com.csc.job.core.handler.IJobHandler;
import com.csc.job.core.log.CscJobFileAppender;
import com.csc.job.core.log.CscJobLog;
import com.csc.job.core.util.ScriptUtil;

import java.io.File;

/**
 * @Description:
 * @PackageName: com.csc.job.core.handler.impl
 * @Author: 陈世超
 * @Create: 2020-10-15 17:36
 * @Version: 1.0
 */
public class ScriptJobHandler extends IJobHandler {
    private int jobId;
    private long glueUpdatetime;
    private String gluesource;
    private GlueTypeEnum glueType;

    public ScriptJobHandler(int jobId, long glueUpdatetime, String gluesource, GlueTypeEnum glueType) {
        this.jobId = jobId;
        this.glueUpdatetime = glueUpdatetime;
        this.gluesource = gluesource;
        this.glueType = glueType;

        File glueSrcPath = new File(CscJobFileAppender.getGlueSrcPath());
        if (glueSrcPath.exists()) {
            File[] glueSrcFileList = glueSrcPath.listFiles();
            if (glueSrcFileList != null && glueSrcFileList.length > 0) {
                for (File glueSrcFileItem : glueSrcFileList) {
                    if (glueSrcFileItem.getName().startsWith(jobId + "_")) {
                        glueSrcFileItem.delete();
                    }
                }
            }
        }
    }

    @Override
    public ReturnT<String> execute(String param) {
        if (!glueType.isScript()) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "glueType[" + glueType + "] invalid.");
        }
        String cmd = glueType.getCmd();

        String scriptFileName = CscJobFileAppender.getGlueSrcPath()
                .concat(File.separator).concat(String.valueOf(jobId)).concat("_")
                .concat(String.valueOf(glueUpdatetime)).concat(glueType.getSuffix());
        File scriptFile = new File(scriptFileName);
        if (!scriptFile.exists()) {
            ScriptUtil.markScriptFile(scriptFileName, gluesource);
        }
        String logFileName = CscJobContext.getCscJobContext().getJobLogFileName();

        // script params: 0=param，1=分片序号，2=分片总数
        String[] scriptParams = new String[3];
        scriptParams[0] = param;
        scriptParams[1] = String.valueOf(CscJobContext.getCscJobContext().getShardIndex());
        scriptParams[2] = String.valueOf(CscJobContext.getCscJobContext().getShardTotal());
        CscJobLog.log("----------- script file:" + scriptFileName + " -----------");
        int exitValue = ScriptUtil.execToFile(cmd, scriptFileName, logFileName, scriptParams);
        if (exitValue == 0) {
            return IJobHandler.SUCCESS;
        } else {
            return new ReturnT<>(IJobHandler.FAIL.getCode(), "script exit value(" + exitValue + ") is failed");
        }
    }


    public long getGlueUpdatetime() {
        return glueUpdatetime;
    }
}
