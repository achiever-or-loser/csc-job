package com.csc.job.core.thread;

import com.csc.job.core.biz.model.HandleCallbackParam;
import com.csc.job.core.biz.model.ReturnT;
import com.csc.job.core.biz.model.TriggerParam;
import com.csc.job.core.context.CscJobContext;
import com.csc.job.core.executor.CscJobExecutor;
import com.csc.job.core.handler.IJobHandler;
import com.csc.job.core.log.CscJobFileAppender;
import com.csc.job.core.log.CscJobLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @Description:
 * @PackageName: com.csc.job.core.thread
 * @Author: 陈世超
 * @Create: 2020-10-14 18:00
 * @Version: 1.0
 */
public class JobThread extends Thread {
    private static Logger logger = LoggerFactory.getLogger(JobThread.class);

    private int jobId;
    private IJobHandler handler;
    private LinkedBlockingDeque<TriggerParam> triggerQueue;
    private Set<Long> triggerLogSet;

    private volatile boolean toStop = false;
    private String stopReason;

    private boolean running = false;
    private int idleTimes = 0;

    public JobThread(int jobId, IJobHandler handler) {
        this.jobId = jobId;
        this.handler = handler;
        this.triggerQueue = new LinkedBlockingDeque<>();
        this.triggerLogSet = Collections.synchronizedSet(new HashSet<>());
    }

    public IJobHandler getHandler() {
        return handler;
    }

    public ReturnT<String> pushTriggerQueue(TriggerParam triggerParam) {
        if (triggerLogSet.contains(triggerParam.getLogId())) {
            logger.info(">>>>>>> repeate trigger job,logId:{}", triggerParam.getLogId());
            return new ReturnT<>(ReturnT.FAIL_CODE, "repeate trigger job,logId:" + triggerParam.getLogId());
        }
        triggerLogSet.add(triggerParam.getLogId());
        triggerQueue.add(triggerParam);
        return ReturnT.SUCCESS;
    }

    public void toStop(String stopReason) {
        this.toStop = true;
        this.stopReason = stopReason;
    }

    public boolean isRunningOrHasQueue() {
        return running || triggerQueue.size() > 0;
    }

    @Override
    public void run() {
        try {
            handler.init();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
        while (!toStop) {
            running = false;
            idleTimes++;

            TriggerParam triggerParam = null;
            ReturnT<String> executeResult = null;
            try {
                triggerParam = triggerQueue.poll(3, TimeUnit.SECONDS);
                if (triggerParam != null) {
                    running = true;
                    idleTimes = 0;
                    triggerLogSet.remove(triggerParam.getLogId());
                    String logFileName = CscJobFileAppender.makeLogFileName(new Date(triggerParam.getLogDateTime()), triggerParam.getLogId());
                    CscJobContext.setCscJobContext(new CscJobContext(triggerParam.getLogId(), logFileName, triggerParam.getBroadcastIndex(), triggerParam.getBroadcastTotal()));
                    CscJobLog.log("<br>----------- csc-job job execute start -----------<br>----------- Param:" + triggerParam.getExecutorParams());
                    if (triggerParam.getExecutorTimeout() > 0) {
                        Thread futureThread = null;

                        try {
                            final TriggerParam triggerParamTmp = triggerParam;
                            FutureTask<ReturnT<String>> futureTask = new FutureTask<>(() -> {
                                return handler.execute(triggerParamTmp.getExecutorParams());
                            });
                            futureThread = new Thread(futureTask);
                            futureThread.start();
                            executeResult = futureTask.get(triggerParam.getExecutorTimeout(), TimeUnit.SECONDS);
                        } catch (TimeoutException e) {
                            CscJobLog.log("<br>----------- csc-job job execute timeout");
                            CscJobLog.log(e);
                            executeResult = new ReturnT<String>(IJobHandler.FAIL_TIMEOUT.getCode(), "job execute timeout ");
                        } finally {
                            futureThread.interrupt();
                        }
                    } else {
                        executeResult = handler.execute(triggerParam.getExecutorParams());
                    }
                    if (executeResult == null) {
                        executeResult = IJobHandler.FAIL;
                    } else {
                        executeResult.setMessage(
                                (executeResult.getMessage() != null && executeResult.getMessage().length() > 50000)
                                        ? executeResult.getMessage().substring(0, 50000).concat("...")
                                        : executeResult.getMessage());
                        executeResult.setContent(null);
                    }
                    CscJobLog.log("<br>----------- csc-job job execute end(finish) -----------<br>----------- ReturnT:" + executeResult);
                } else {
                    if (idleTimes > 30) {
                        if (triggerQueue.size() == 0) {
                            CscJobExecutor.removeJobThread(jobId, "executor idle times over limit.");
                        }
                    }
                }
            } catch (Exception e) {
                if (toStop) {
                    CscJobLog.log("<br>----------- JobThread toStop, stopReason:" + stopReason);
                }
                StringWriter stringWriter = new StringWriter();
                e.printStackTrace(new PrintWriter(stringWriter));
                String errorMsg = stringWriter.toString();
                executeResult = new ReturnT<>(ReturnT.FAIL_CODE, errorMsg);

                CscJobLog.log("<br>----------- JobThread Exception:" + errorMsg + "<br>----------- csc-job job execute end(error) -----------");
            } finally {
                if (triggerParam != null) {
                    if (!toStop) {
                        TriggerCallbackThread.pushCallBack(new HandleCallbackParam(triggerParam.getLogId(), triggerParam.getLogDateTime(), executeResult));
                    } else {
                        ReturnT<String> stopResult = new ReturnT<>(ReturnT.FAIL_CODE, stopReason + " [job running, killed]");
                        TriggerCallbackThread.pushCallBack(new HandleCallbackParam(triggerParam.getLogId(), triggerParam.getLogDateTime(), stopResult));
                    }
                }
            }
        }
        while (triggerQueue != null && triggerQueue.size() > 0) {
            TriggerParam triggerParam = triggerQueue.poll();
            if (triggerParam != null) {
                ReturnT<String> stopResult = new ReturnT<String>(ReturnT.FAIL_CODE, stopReason + " [job not executed, in the job queue, killed.]");
                TriggerCallbackThread.pushCallBack(new HandleCallbackParam(triggerParam.getLogId(), triggerParam.getLogDateTime(), stopResult));
            }
        }
        try {
            handler.destroy();
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }

        logger.info(">>>>>>>>>>> csc-job JobThread stoped, hashCode:{}", Thread.currentThread());
    }

}
