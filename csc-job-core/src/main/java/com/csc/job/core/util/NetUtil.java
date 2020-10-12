package com.csc.job.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * @Description:
 * @PackageName: com.csc.job.core.util
 * @Author: 陈世超
 * @Create: 2020-10-12 13:35
 * @Version: 1.0
 */
public class NetUtil {
    private static Logger logger = LoggerFactory.getLogger(NetUtil.class);

    public static int findAvailableport(int defaultPort) {
        int port = defaultPort;
        while (port < 65535) {
            if (!isPortUsed(port)) {
                return port;
            } else {
                port++;
            }
        }
        port = defaultPort--;
        while (port > 0) {
            if (!isPortUsed(port)) {
                return port;
            } else {
                port--;
            }
        }
        throw new RuntimeException("no available port.");
    }

    public static boolean isPortUsed(int port) {
        boolean used = false;
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            used = false;
        } catch (IOException e) {
            logger.info(">>>>>>>>> csc-rpc,port[{}] is in used.", port);
            used = true;
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return used;
    }
}
