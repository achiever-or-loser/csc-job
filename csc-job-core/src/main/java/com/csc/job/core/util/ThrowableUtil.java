package com.csc.job.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @Description:
 * @PackageName: com.csc.job.core.util
 * @Author: 陈世超
 * @Create: 2020-10-12 14:19
 * @Version: 1.0
 */
public class ThrowableUtil {

    public static String toString(Throwable throwable) {
        StringWriter stringWriter = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stringWriter));
        String errorMsg = stringWriter.toString();
        return errorMsg;
    }
}
