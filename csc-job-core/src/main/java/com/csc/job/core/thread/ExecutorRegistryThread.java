package com.csc.job.core.thread;

import com.csc.job.core.biz.AdminBiz;
import com.csc.job.core.biz.model.RegistryParam;
import com.csc.job.core.biz.model.ReturnT;
import com.csc.job.core.enums.RegistryConfig;
import com.csc.job.core.executor.CscJobExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @PackageName: com.csc.job.core.thread
 * @Author: 陈世超
 * @Create: 2020-10-12 17:17
 * @Version: 1.0
 */
public class ExecutorRegistryThread {
    private static Logger logger = LoggerFactory.getLogger(ExecutorRegistryThread.class);

    private static ExecutorRegistryThread instance = new ExecutorRegistryThread();

    public static ExecutorRegistryThread getInstance() {
        return instance;
    }

    private Thread registThread;
    private volatile boolean toStop = false;

    public void start(String appName, final String address) {
        if (appName == null || appName.trim().length() == 0) {
            logger.warn(">>>>>>>csc-job,executor registry config fail,appName is null.");
            return;
        }
        if (CscJobExecutor.getAdminBizList() == null) {
            logger.warn(">>>>>>>>>>> csc-job, executor registry config fail, adminAddresses is null.");
            return;
        }
        registThread = new Thread(() -> {
            while (!toStop) {
                try {
                    RegistryParam registryParam = new RegistryParam(RegistryConfig.RegistryType.EXECUTOR.name(), appName, address);
                    for (AdminBiz adminBiz : CscJobExecutor.getAdminBizList()) {
                        try {
                            ReturnT<String> registryResult = adminBiz.registry(registryParam);
                            if (registryResult != null && ReturnT.SUCCESS_CODE == registryResult.getCode()) {
                                registryResult = ReturnT.SUCCESS;
                                logger.debug(">>>>>>>>>>> csc-job registry success, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                                break;
                            } else {
                                logger.info(">>>>>>>>>>> csc-job registry fail, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                            }
                        } catch (Exception e) {
                            logger.info(">>>>>>>>>>> csc-job registry error, registryParam:{}", registryParam, e);
                        }
                    }
                } catch (Exception e) {
                    if (!toStop) logger.error(e.getMessage(), e);

                }
                try {
                    if (!toStop) {
                        TimeUnit.SECONDS.sleep(RegistryConfig.BEAT_TIMEOUT);
                    }
                } catch (InterruptedException e) {
                    if (!toStop) {
                        logger.warn(">>>>>>>>>>> csc-job, executor registry thread interrupted, error msg:{}", e.getMessage());
                    }
                }
            }
            try {
                RegistryParam registryParam = new RegistryParam(RegistryConfig.RegistryType.EXECUTOR.name(), appName, address);
                for (AdminBiz adminBiz : CscJobExecutor.getAdminBizList()) {
                    try {
                        ReturnT<String> registryResult = adminBiz.registryRemove(registryParam);
                        if (registryResult != null && ReturnT.SUCCESS_CODE == registryResult.getCode()) {
                            registryResult = ReturnT.SUCCESS;
                            logger.info(">>>>>>>>>>> csc-job registry-remove success, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                            break;
                        } else {
                            logger.info(">>>>>>>>>>> csc-job registry-remove fail, registryParam:{}, registryResult:{}", new Object[]{registryParam, registryResult});
                        }
                    } catch (Exception e) {
                        if (!toStop) {
                            logger.info(">>>>>>>>>>> csc-job registry-remove error, registryParam:{}", registryParam, e);
                        }

                    }

                }
            } catch (Exception e) {
                if (!toStop) logger.error(e.getMessage(), e);

            }
            logger.info(">>>>>>>>>>> csc-job, executor registry thread destory.");
        }, "csc-job, executor ExecutorRegistryThread");
        registThread.setDaemon(true);
        registThread.start();
    }

    public void toStop() {
        toStop = true;
        registThread.interrupt();
        try {
            registThread.join();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
