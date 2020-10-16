package com.csc.job.core.biz.model;

import java.io.Serializable;

/**
 * @Description:
 * @PackageName: com.csc.job.core.biz.model
 * @Author: 陈世超
 * @Create: 2020-10-13 13:17
 * @Version: 1.0
 */
public class RegistryParam implements Serializable {
    private static final long serialVersionUID = 42L;

    private String registryGroup;
    private String registrykey;
    private String registryValue;

    public RegistryParam() {
    }

    public RegistryParam(String registryGroup, String registrykey, String registryValue) {
        this.registryGroup = registryGroup;
        this.registrykey = registrykey;
        this.registryValue = registryValue;
    }

    public String getRegistryGroup() {
        return registryGroup;
    }

    public void setRegistryGroup(String registryGroup) {
        this.registryGroup = registryGroup;
    }

    public String getRegistrykey() {
        return registrykey;
    }

    public void setRegistrykey(String registrykey) {
        this.registrykey = registrykey;
    }

    public String getRegistryValue() {
        return registryValue;
    }

    public void setRegistryValue(String registryValue) {
        this.registryValue = registryValue;
    }

    @Override
    public String toString() {
        return "RegistryParam{" +
                "registryGroup='" + registryGroup + '\'' +
                ", registrykey='" + registrykey + '\'' +
                ", registryValue='" + registryValue + '\'' +
                '}';
    }
}
