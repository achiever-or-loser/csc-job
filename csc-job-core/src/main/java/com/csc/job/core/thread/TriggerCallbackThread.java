package com.csc.job.core.thread;

import com.csc.job.core.biz.AdminBiz;
import com.csc.job.core.biz.model.HandleCallbackParam;
import com.csc.job.core.biz.model.ReturnT;
import com.csc.job.core.context.CscJobContext;
import com.csc.job.core.enums.RegistryConfig;
import com.csc.job.core.executor.CscJobExecutor;
import com.csc.job.core.log.CscJobFileAppender;
import com.csc.job.core.log.CscJobLog;
import com.csc.job.core.util.FileUtil;
import com.csc.job.core.util.JDKSerializeTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @PackageName: com.csc.job.core.thread
 * @Author: 陈世超
 * @Create: 2020-10-13 14:00
 * @Version: 1.0
 */
public class TriggerCallbackThread {
    private static Logger logger = LoggerFactory.getLogger(TriggerCallbackThread.class);
    private static TriggerCallbackThread instance = new TriggerCallbackThread();

    public static TriggerCallbackThread getInstance() {
        return instance;
    }

    private LinkedBlockingDeque<HandleCallbackParam> callbackQueue = new LinkedBlockingDeque<>();

    public static void pushCallBack(HandleCallbackParam callbackParam) {
        getInstance().callbackQueue.add(callbackParam);
        logger.debug(">>>>> csc-job, push callback request, logId:{}", callbackParam.getLogId());
    }

    private Thread triggerCallbackThread;
    private Thread triggerRetryCallbackThread;
    private volatile boolean toStop = false;

    public void start() {
        if (CscJobExecutor.getAdminBizList() == null) {
            logger.warn(">>>>>>>>>>> csc-job, executor callback config fail, adminAddresses is null.");
            return;
        }
        triggerCallbackThread = new Thread(() -> {
            while (!toStop) {
                try {
                    HandleCallbackParam callbackParam = getInstance().callbackQueue.take();
                    if (callbackParam != null) {
                        List<HandleCallbackParam> callbackParamList = new ArrayList<>();
                        getInstance().callbackQueue.drainTo(callbackParamList);
                        callbackParamList.add(callbackParam);
                        if (callbackParamList != null && callbackParamList.size() > 0) {
                            doCallback(callbackParamList);
                        }

                    }
                } catch (Exception e) {
                    if (!toStop) logger.error(e.getMessage(), e);
                }
            }
            try {
                List<HandleCallbackParam> callbackParamList = new ArrayList<>();
                getInstance().callbackQueue.drainTo(callbackParamList);
                if (callbackParamList != null && callbackParamList.size() > 0) {
                    doCallback(callbackParamList);
                }
            } catch (Exception e) {
                if (!toStop) logger.error(e.getMessage(), e);
            }
        }, "csc-job, executor TriggerCallbackThread");
        triggerCallbackThread.setDaemon(true);
        triggerCallbackThread.start();

        triggerRetryCallbackThread = new Thread(() -> {
            while (!toStop) {
                try {
                    retryFailCallbackFile();
                } catch (Exception e) {
                    if (!toStop) logger.error(e.getMessage(), e);
                }
                try {
                    TimeUnit.SECONDS.sleep(RegistryConfig.BEAT_TIMEOUT);
                } catch (InterruptedException e) {
                    if (!toStop) logger.error(e.getMessage(), e);
                }
            }
            logger.info(">>>>>>>>>>> csc-job, executor retry callback thread destory.");
        });
        triggerRetryCallbackThread.setDaemon(true);
        triggerRetryCallbackThread.start();

    }

    public void toStop() {
        toStop = true;
        if (triggerCallbackThread != null) {
            triggerCallbackThread.interrupt();
            try {
                triggerCallbackThread.join();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }

        if (triggerRetryCallbackThread != null) {
            triggerRetryCallbackThread.interrupt();
            try {
                triggerRetryCallbackThread.join();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void doCallback(List<HandleCallbackParam> callbackParamList) {
        boolean callbackRet = false;
        for (AdminBiz adminBiz : CscJobExecutor.getAdminBizList()) {
            try {
                ReturnT<String> callbackResult = adminBiz.callback(callbackParamList);
                if (callbackResult != null && callbackResult.getCode() == ReturnT.SUCCESS_CODE) {
                    callbackLog(callbackParamList, "<br>----------- csc-job job callback finish.");
                    callbackRet = true;
                    break;
                } else {
                    callbackLog(callbackParamList, "<br>----------- csc-job job callback fail, callbackResult:" + callbackResult);
                }
            } catch (Exception e) {
                callbackLog(callbackParamList, "<br>----------- csc-job job callback error, errorMsg:" + e.getMessage());
            }
        }
        if (!callbackRet) {
            appendFailCallbackFile(callbackParamList);
        }
    }

    private void callbackLog(List<HandleCallbackParam> callbackParamList, String logContent) {
        for (HandleCallbackParam callbackParam : callbackParamList) {
            String logFileName = CscJobFileAppender.makeLogFileName(new Date(callbackParam.getLogDateTim()), callbackParam.getLogId());
            CscJobContext.setCscJobContext(new CscJobContext(-1, logFileName, -1, -1));
            CscJobLog.log(logContent);
        }
    }

    private static String failCallbackFilePath = CscJobFileAppender.getLogBasePath().concat(File.separator).concat("callbacklog").concat(File.separator);
    private static String failCallbackFileName = failCallbackFilePath.concat("csc-job-callback-{x}").concat(".log");

    private void appendFailCallbackFile(List<HandleCallbackParam> callbackParamList) {
        if (callbackParamList == null && callbackParamList.size() == 0) return;
        byte[] callbackParamList_bytes = JDKSerializeTool.serialize(callbackParamList);
        File callbackLogFile = new File(failCallbackFileName.replace("{x}", String.valueOf(System.currentTimeMillis())));
        if (callbackLogFile.exists()) {
            for (int i = 0; i < 100; i++) {
                callbackLogFile = new File(failCallbackFileName.replace("{x}", String.valueOf(System.currentTimeMillis()).concat("-").concat(String.valueOf(i))));
                if (!callbackLogFile.exists()) break;
            }
        }
        FileUtil.writeFileContent(callbackLogFile, callbackParamList_bytes);
    }

    private void retryFailCallbackFile() {
        File callbackLogPath = new File(failCallbackFilePath);
        if (!callbackLogPath.exists()) return;
        if (callbackLogPath.isFile()) callbackLogPath.delete();
        if (!(callbackLogPath.isDirectory() && callbackLogPath.list() != null && callbackLogPath.list().length > 0))
            return;
        for (File callbackLogFile : callbackLogPath.listFiles()) {
            byte[] callbackParamList_bytes = FileUtil.readFileContent(callbackLogFile);
            List<HandleCallbackParam> callbackParamList = (List<HandleCallbackParam>) JDKSerializeTool.deserialize(callbackParamList_bytes, List.class);

            callbackLogFile.delete();
            doCallback(callbackParamList);
        }
    }
}
