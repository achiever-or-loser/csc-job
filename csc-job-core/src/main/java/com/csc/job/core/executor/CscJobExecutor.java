package com.csc.job.core.executor;

import com.csc.job.core.biz.AdminBiz;
import com.csc.job.core.biz.client.AdminBizClient;
import com.csc.job.core.handler.IJobHandler;
import com.csc.job.core.log.CscJobFileAppender;
import com.csc.job.core.server.EmbedServer;
import com.csc.job.core.thread.JobLogFileCleanThread;
import com.csc.job.core.thread.JobThread;
import com.csc.job.core.thread.TriggerCallbackThread;
import com.csc.job.core.util.IpUtil;
import com.csc.job.core.util.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Description:
 * @PackageName: com.csc.job.core.executor
 * @Author: 陈世超
 * @Create: 2020-10-12 17:23
 * @Version: 1.0
 */
public class CscJobExecutor {
    private static Logger logger = LoggerFactory.getLogger(CscJobExecutor.class);

    private String adminAddresses;
    private String accessToken;
    private String appName;
    private String address;
    private String ip;
    private int port;
    private String logPath;
    private int logRetentionDays;

    public void start() {
        CscJobFileAppender.initLogPath(logPath);
        initAdminBizList(adminAddresses, accessToken);
        JobLogFileCleanThread.getInstance().start(logRetentionDays);
        TriggerCallbackThread.getInstance().start();
        initEmbedServer(address, ip, port, appName, accessToken);
    }

    public void destroy() {
        stopEmbedServer();
        if (jobThreadRepository.size() > 0) {
            for (Map.Entry<Integer, JobThread> item : jobThreadRepository.entrySet()) {
                JobThread oldJobThread = removeJobThread(item.getKey(), "web container destroy and kill the job.");
                if (oldJobThread != null) {
                    try {
                        oldJobThread.join();
                    } catch (InterruptedException e) {
                        logger.error(">>>>>>>>>>> csc-job, JobThread destroy(join) error, jobId:{}", item.getKey(), e);
                    }
                }
            }
            jobThreadRepository.clear();
        }
        jobHandlerRepository.clear();
        JobLogFileCleanThread.getInstance().toStop();
        TriggerCallbackThread.getInstance().toStop();
    }

    private static List<AdminBiz> adminBizList;

    public void initAdminBizList(String adminAddresses, String accessToken) {
        if (adminAddresses != null && adminAddresses.trim().length() > 0) {
            for (String address : adminAddresses.split(",")) {
                if (address != null && address.trim().length() > 0) {
                    AdminBiz adminBiz = new AdminBizClient(address.trim(), accessToken);
                    if (adminBizList == null) {
                        adminBizList = new ArrayList<>();
                    }
                    adminBizList.add(adminBiz);
                }
            }
        }
    }


    private EmbedServer embedServer = null;

    private void initEmbedServer(String address, String ip, int port, String appName, String accessToken) {
        port = port > 0 ? port : NetUtil.findAvailableport(9999);
        ip = (ip != null && ip.trim().length() > 0) ? ip : IpUtil.getIp();

        if (address == null && address.trim().length() == 0) {
            String ip_port_address = IpUtil.getIpPort(ip, port);
            address = "http://{ip_port}".replace("ip_port", ip_port_address);
        }
        embedServer = new EmbedServer();
        embedServer.start(address, port, appName, accessToken);
    }

    private void stopEmbedServer() {
        embedServer.toStop();
    }


    private static ConcurrentHashMap<String, IJobHandler> jobHandlerRepository = new ConcurrentHashMap<>();

    public static IJobHandler registJobHandler(String name, IJobHandler jobHandler) {
        logger.info(">>>>>>>>>>> csc-job register jobhandler success, name:{}, jobHandler:{}", name, jobHandler);
        return jobHandlerRepository.put(name, jobHandler);
    }

    public static IJobHandler loadJobHandler(String name) {
        return jobHandlerRepository.get(name);
    }


    private static ConcurrentHashMap<Integer, JobThread> jobThreadRepository = new ConcurrentHashMap<>();

    public static JobThread registJobThread(int jobId, IJobHandler jobHandler, String removeOldReason) {
        JobThread jobThread = new JobThread(jobId, jobHandler);
        jobThread.start();
        logger.info(">>>>>>>>>>> csc-job regist JobThread success, jobId:{}, handler:{}", new Object[]{jobId, jobHandler});
        JobThread oldJobThread = jobThreadRepository.put(jobId, jobThread);
        if (oldJobThread != null) {
            oldJobThread.toStop(removeOldReason);
            oldJobThread.interrupt();
        }
        return jobThread;
    }

    public static JobThread removeJobThread(int jobId, String removeOldReason) {
        JobThread oldJobThread = jobThreadRepository.remove(jobId);
        if (oldJobThread != null) {
            oldJobThread.toStop(removeOldReason);
            oldJobThread.interrupt();
            return oldJobThread;
        }
        return null;
    }

    public static JobThread loadJobThread(int jobId) {
        return jobThreadRepository.get(jobId);
    }


    public static List<AdminBiz> getAdminBizList() {
        return adminBizList;
    }

    public void setAdminAddresses(String adminAddresses) {
        this.adminAddresses = adminAddresses;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public void setLogRetentionDays(int logRetentionDays) {
        this.logRetentionDays = logRetentionDays;
    }
}
