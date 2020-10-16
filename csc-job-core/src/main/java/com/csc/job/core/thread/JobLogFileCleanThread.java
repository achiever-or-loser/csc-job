package com.csc.job.core.thread;

import com.csc.job.core.log.CscJobFileAppender;
import com.csc.job.core.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @Description:
 * @PackageName: com.csc.job.core.thread
 * @Author: 陈世超
 * @Create: 2020-10-13 13:34
 * @Version: 1.0
 */
public class JobLogFileCleanThread {
    private static Logger logger = LoggerFactory.getLogger(JobLogFileCleanThread.class);
    private static JobLogFileCleanThread instance = new JobLogFileCleanThread();

    public static JobLogFileCleanThread getInstance() {
        return instance;
    }

    private Thread localThread;
    private volatile boolean toStop = false;

    public void start(final long logRetentionDays) {
        if (logRetentionDays < 3) return;

        localThread = new Thread(() -> {
            while (!toStop) {
                try {
                    File[] childDirs = new File(CscJobFileAppender.getLogBasePath()).listFiles();
                    if (childDirs != null && childDirs.length > 0) {
                        Calendar todayCal = Calendar.getInstance();
                        todayCal.set(Calendar.HOUR_OF_DAY, 0);
                        todayCal.set(Calendar.MINUTE, 0);
                        todayCal.set(Calendar.SECOND, 0);
                        todayCal.set(Calendar.MILLISECOND, 0);

                        Date todayDate = todayCal.getTime();
                        for (File childFile : childDirs) {
                            if (!childFile.isDirectory()) continue;
                            if (childFile.getName().indexOf("-") == -1) continue;

                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                            Date logFileCreateDate = simpleDateFormat.parse(childFile.getName());
                            if (logFileCreateDate == null) continue;
                            if ((todayDate.getTime() - logFileCreateDate.getTime()) >= logRetentionDays * (24 * 60 * 60 * 1000)) {
                                FileUtil.deleteRecursively(childFile);
                            }
                        }
                    }
                } catch (Exception e) {
                    if (!toStop) {
                        logger.error(e.getMessage(), e);
                    }

                }
            }
            try {
                TimeUnit.DAYS.sleep(1);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }, "csc-job, executor JobLogFileCleanThread");
        localThread.setDaemon(true);
        localThread.start();
    }

    public void toStop() {
        toStop = true;
        if (localThread == null) return;
        localThread.interrupt();
        try {
            localThread.join();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
