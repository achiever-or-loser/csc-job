package com.csc.job.core.log;

import com.csc.job.core.context.CscJobContext;
import com.csc.job.core.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

/**
 * @Description:
 * @PackageName: com.csc.job.core.log
 * @Author: 陈世超
 * @Create: 2020-10-14 14:28
 * @Version: 1.0
 */
public class CscJobLog {
    private static Logger logger = LoggerFactory.getLogger("csc-job logger");

    private static void logDetail(StackTraceElement callInfo, String appendLog) {
        /*// "yyyy-MM-dd HH:mm:ss [ClassName]-[MethodName]-[LineNumber]-[ThreadName] log";
        StackTraceElement[] stackTraceElements = new Throwable().getStackTrace();
        StackTraceElement callInfo = stackTraceElements[1];*/
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(DateUtil.formatDateTime(new Date())).append(" ")
                .append("[").append(callInfo.getClassName()).append("#").append(callInfo.getMethodName()).append("]-")
                .append("[").append(callInfo.getLineNumber()).append("]-")
                .append("[").append(Thread.currentThread().getName()).append("] ")
                .append(appendLog != null ? appendLog : "");
        String formateAppendLog = stringBuffer.toString();
        String logFileName = CscJobContext.getCscJobContext().getJobLogFileName();
        if (logFileName != null && logFileName.trim().length() > 0) {
            CscJobFileAppender.appendLog(logFileName, formateAppendLog);
        } else {
            logger.info(">>>>>> {}", formateAppendLog);
        }
    }

    public static void log(String appendLogPattern, Object... appendLogArguments) {
        FormattingTuple formattingTuple = MessageFormatter.arrayFormat(appendLogPattern, appendLogArguments);
        String appendLog = formattingTuple.getMessage();

        StackTraceElement callInfo = new Throwable().getStackTrace()[1];
        logDetail(callInfo, appendLog);
    }

    public static void log(Throwable e) {
        StringWriter stringWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stringWriter));
        String appendLog = stringWriter.toString();

        StackTraceElement callInfo = new Throwable().getStackTrace()[1];
        logDetail(callInfo, appendLog);
    }
}
