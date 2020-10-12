package com.csc.job.core.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description:
 * @PackageName: com.csc.job.core.util
 * @Author: 陈世超
 * @Create: 2020-10-12 13:49
 * @Version: 1.0
 */
public class ScriptUtil {

    public static void markScriptFile(String scriptFileName, String content) {
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(scriptFileName);
            fileOutputStream.write(content.getBytes("UTF-8"));
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static int execToFile(String command, String scriptFile, String logFile, String... params) {
        FileOutputStream fileOutputStream = null;
        Thread inputThread = null;
        Thread errThread = null;
        try {
            fileOutputStream = new FileOutputStream(logFile, true);

            List<String> cmdArray = new ArrayList<>();
            cmdArray.add(command);
            cmdArray.add(scriptFile);
            if (params != null && params.length > 0) {
                for (String param : params) {
                    cmdArray.add(param);
                }
            }
            String[] cmdArrayFinal = cmdArray.toArray(new String[cmdArray.size()]);

            final Process process = Runtime.getRuntime().exec(cmdArrayFinal);
            final FileOutputStream finalFileOutputStream = fileOutputStream;
            inputThread = new Thread(() -> {
                copy(process.getInputStream(), finalFileOutputStream, new byte[1024]);
            });
            errThread = new Thread(() -> {
                copy(process.getErrorStream(), finalFileOutputStream, new byte[1024]);
            });
            inputThread.start();
            errThread.start();
            int exitValue = process.waitFor();

            inputThread.join();
            errThread.join();
            return exitValue;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputThread != null && inputThread.isAlive()) {
                inputThread.interrupt();
            }
            if (errThread != null && errThread.isAlive()) {
                errThread.interrupt();
            }
        }
    }

    public static long copy(InputStream inputStream, OutputStream outputStream, byte[] buffer) {
        try {
            long total = 0;
            for (; ; ) {
                int res = inputStream.read(buffer);
                if (res == -1) break;
                if (res > 0) {
                    total += res;
                    if (outputStream != null) {
                        outputStream.write(buffer, 0, res);
                    }
                }
            }
            outputStream.flush();
            inputStream.close();
            inputStream = null;
            return total;
        } catch (Exception e) {

        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }
}
