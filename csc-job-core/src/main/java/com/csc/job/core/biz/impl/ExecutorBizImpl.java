package com.csc.job.core.biz.impl;

import com.csc.job.core.biz.ExecutorBiz;
import com.csc.job.core.biz.model.*;
import com.csc.job.core.enums.ExecutorBlockStrategyEnum;
import com.csc.job.core.executor.CscJobExecutor;
import com.csc.job.core.glue.GlueFactory;
import com.csc.job.core.glue.GlueTypeEnum;
import com.csc.job.core.handler.IJobHandler;
import com.csc.job.core.handler.impl.GlueJobHandler;
import com.csc.job.core.handler.impl.ScriptJobHandler;
import com.csc.job.core.log.CscJobFileAppender;
import com.csc.job.core.thread.JobThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * @Description:
 * @PackageName: com.csc.job.core.biz.impl
 * @Author: 陈世超
 * @Create: 2020-10-14 17:58
 * @Version: 1.0
 */
public class ExecutorBizImpl implements ExecutorBiz {
    private static Logger logger = LoggerFactory.getLogger(ExecutorBizImpl.class);

    @Override
    public ReturnT<String> beat() {
        return ReturnT.SUCCESS;
    }

    @Override
    public ReturnT<String> idleBeat(IdleBeatParam idleBeatParam) {
        boolean isRunningOrHasQueue = false;
        JobThread jobThread = CscJobExecutor.loadJobThread(idleBeatParam.getJobId());
        if (jobThread != null && jobThread.isRunningOrHasQueue()) {
            isRunningOrHasQueue = true;
        }
        if (isRunningOrHasQueue) {
            return new ReturnT<>(ReturnT.FAIL_CODE, "job thread is running or has trigger queue.");
        }
        return null;
    }

    @Override
    public ReturnT<String> run(TriggerParam triggerParam) {
        JobThread jobThread = CscJobExecutor.loadJobThread(triggerParam.getJobId());
        IJobHandler jobHandler = jobThread != null ? jobThread.getHandler() : null;
        String removeOldReason = null;
        GlueTypeEnum glueTypeEnum = GlueTypeEnum.match(triggerParam.getGlueType());
        if (GlueTypeEnum.BEAN == glueTypeEnum) {
            IJobHandler newJobHandler = CscJobExecutor.loadJobHandler(triggerParam.getExecutorHandler());

            if (jobHandler != null && jobHandler != newJobHandler) {
                removeOldReason = "change jobhandler or glue type, and terminate the old job thread.";
                jobHandler = null;
                jobThread = null;
            }
            if (jobHandler == null) {
                jobHandler = newJobHandler;
                if (jobHandler == null) {
                    return new ReturnT<>(ReturnT.FAIL_CODE, "job handler [" + triggerParam.getExecutorHandler() + "] not found.");
                }
            }
        } else if (GlueTypeEnum.GLUE_GROOVY == glueTypeEnum) {
            if (jobThread != null &&
                    !(jobThread.getHandler() instanceof GlueJobHandler
                            && (((GlueJobHandler) jobThread.getHandler()).getGlueUpdatetime() == triggerParam.getGlueUpdatetime()))) {
                removeOldReason = "change job source or glue type, and terminate the old job thread.";
                jobHandler = null;
                jobThread = null;
            }
            if (jobHandler == null) {
                try {
                    IJobHandler originJobHandler = GlueFactory.getInstance().loadNewInstance(triggerParam.getGlueSource());
                    jobHandler = new GlueJobHandler(triggerParam.getGlueUpdatetime(), originJobHandler);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return new ReturnT<String>(ReturnT.FAIL_CODE, e.getMessage());
                }
            }
        } else if (glueTypeEnum != null && glueTypeEnum.isScript()) {
            if (jobThread != null &&
                    !(jobThread.getHandler() instanceof ScriptJobHandler)
                    && ((ScriptJobHandler) jobThread.getHandler()).getGlueUpdatetime() == triggerParam.getGlueUpdatetime()) {
                removeOldReason = "change job source or glue type, and terminate the old job thread.";
                jobThread = null;
                jobHandler = null;
            }
            if (jobHandler == null) {
                jobHandler = new ScriptJobHandler(triggerParam.getJobId(), triggerParam.getGlueUpdatetime(),
                        triggerParam.getGlueSource(), GlueTypeEnum.match(triggerParam.getGlueType()));
            }
        } else {
            return new ReturnT<>(ReturnT.FAIL_CODE, "glueType[" + triggerParam.getGlueType() + "] is not valid.");
        }

        if (jobThread != null) {
            ExecutorBlockStrategyEnum blockStrategy = ExecutorBlockStrategyEnum.match(triggerParam.getExecutorBlockStrategy(), null);
            if (ExecutorBlockStrategyEnum.DISCARD_LATER == blockStrategy) {
                if (jobThread.isRunningOrHasQueue()) {
                    return new ReturnT<>(ReturnT.FAIL_CODE, "block strategy effect：" + ExecutorBlockStrategyEnum.DISCARD_LATER.getTitle());
                }
            } else if (ExecutorBlockStrategyEnum.COVER_EARLY == blockStrategy) {
                if (jobThread.isRunningOrHasQueue()) {
                    removeOldReason = "block strategy effect：" + ExecutorBlockStrategyEnum.COVER_EARLY.getTitle();
                    jobThread = null;
                }
            } else {
            }
            if (jobThread == null) {
                jobThread = CscJobExecutor.registJobThread(triggerParam.getJobId(), jobHandler, removeOldReason);
            }
        }
        return jobThread.pushTriggerQueue(triggerParam);
    }

    @Override
    public ReturnT<String> kill(KillParam killParam) {
        JobThread jobThread = CscJobExecutor.loadJobThread(killParam.getJobId());
        if (jobThread != null) {
            CscJobExecutor.removeJobThread(killParam.getJobId(), "schedule center kill do");
            return ReturnT.SUCCESS;
        }
        return new ReturnT<>(ReturnT.SUCCESS_CODE, "job Thread already killed");
    }

    @Override
    public ReturnT<LogResult> log(LogParam logParam) {
        String logFileName = CscJobFileAppender.makeLogFileName(new Date(logParam.getLogDateTim()), logParam.getLogId());
        LogResult logResult = CscJobFileAppender.readLog(logFileName, logParam.getFromLineNum());
        return new ReturnT<>(logResult);
    }
}
