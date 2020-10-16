package com.csc.job.core.enums;

/**
 * @Description:
 * @PackageName: com.csc.job.core.enums
 * @Author: 陈世超
 * @Create: 2020-10-14 15:16
 * @Version: 1.0
 */
public class RegistryConfig {
    public static final int BEAT_TIMEOUT = 30;
    public static final int DEAD_TIMEOUT = BEAT_TIMEOUT * 3;

    public enum RegistryType {EXECUTOR, ADMIN}
}