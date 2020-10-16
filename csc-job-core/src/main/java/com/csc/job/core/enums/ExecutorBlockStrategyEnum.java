package com.csc.job.core.enums;

/**
 * @Description:
 * @PackageName: com.csc.job.core.enums
 * @Author: 陈世超
 * @Create: 2020-10-15 19:44
 * @Version: 1.0
 */
public enum ExecutorBlockStrategyEnum {
    SERIAL_EXECUTION("serial execution"),
    DISCARD_LATER("discard later"),
    COVER_EARLY("cover early");

    private String title;

    ExecutorBlockStrategyEnum(String title) {
        this.title = title;
    }

    public static ExecutorBlockStrategyEnum match(String name, ExecutorBlockStrategyEnum defaultItem) {
        if (name != null) {
            for (ExecutorBlockStrategyEnum item : ExecutorBlockStrategyEnum.values()) {
                if (item.name().equals(name)) {
                    return item;
                }
            }
        }
        return defaultItem;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
