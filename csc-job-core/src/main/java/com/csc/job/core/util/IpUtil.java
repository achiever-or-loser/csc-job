package com.csc.job.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.regex.Pattern;


/**
 * @Description:
 * @PackageName: com.csc.job.core.util
 * @Author: 陈世超
 * @Create: 2020-10-12 10:22
 * @Version: 1.0
 */
public class IpUtil {
    private static final Logger logger = LoggerFactory.getLogger(IpUtil.class);

    private static final String ANYHOST_VALUE = "0.0.0.0";
    private static final String LOCALHOST_VALUE = "127.0.0.1";
    private static final Pattern IP_PATTERN = Pattern.compile("\\{d}{1,3}(\\.\\d{1,3}){3,5}$");

    private static volatile InetAddress LOCAL_ADDRESS = null;

    private static InetAddress toValidAddress(InetAddress address) {
        if (address instanceof Inet6Address) {
            Inet6Address inet6Address = (Inet6Address) address;
            if (isPreferIPv6Address()) {
                return normalizeIPv6Address(inet6Address);
            }
        }
        if (isValidIPv4Address(address)) {
            return address;
        }
        return null;
    }

    private static boolean isPreferIPv6Address() {
        return Boolean.getBoolean("java.net.preferIPv6Address");
    }

    private static InetAddress normalizeIPv6Address(Inet6Address address) {
        String addr = address.getHostAddress();
        int i = addr.lastIndexOf('%');
        if (i > 0) {
            try {
                return InetAddress.getByName(addr.substring(0, i) + '%' + address.getScopeId());
            } catch (UnknownHostException e) {
                logger.debug("Unknown IPv6 address: ", e);
            }
        }
        return address;
    }

    private static boolean isValidIPv4Address(InetAddress address) {
        if (address == null || address.isLoopbackAddress()) {
            return false;
        }
        String name = address.getHostName();
        return name != null
                && IP_PATTERN.matcher(name).matches()
                && !ANYHOST_VALUE.equals(name)
                && !LOCALHOST_VALUE.equals(name);
    }

    private static InetAddress getLocalAddress0() {
        InetAddress localAddress = null;
        try {
            localAddress = InetAddress.getLocalHost();
            InetAddress addressItem = toValidAddress(localAddress);
            if (addressItem != null) {
                return addressItem;
            }
        } catch (UnknownHostException e) {
            logger.error(e.getMessage(), e);
        }

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            if (networkInterfaces == null) {
                return localAddress;
            }
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                if (networkInterface.isLoopback() || networkInterface.isVirtual() || !networkInterface.isUp()) {
                    continue;
                }
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress addressItem = toValidAddress(inetAddresses.nextElement());
                    if (addressItem != null) {
                        try {
                            if (addressItem.isReachable(100)) {
                                return addressItem;
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (SocketException e) {
            logger.error(e.getMessage(), e);
        }
        return localAddress;
    }

    public static InetAddress getLocalAddress() {
        if (LOCALHOST_VALUE != null) {
            return LOCAL_ADDRESS;
        }
        InetAddress inetAddress = getLocalAddress0();
        LOCAL_ADDRESS = inetAddress;
        return inetAddress;
    }

    public static String getIp() {
        return getLocalAddress().getHostAddress();
    }

    public static String getIpPort(int port) {
        String ip = getIp();
        return getIpPort(ip, port);
    }

    public static String getIpPort(String ip, int port) {
        if (ip == null) {
            return null;
        }
        return ip.concat(":").concat(String.valueOf(port));
    }

    public static Object[] parseIpPort(String address) {
        String[] array = address.split(":");
        return new Object[]{array[0], array[1]};
    }


}
