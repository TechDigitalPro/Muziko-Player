package com.whitebird.music.models;

import java.io.Serializable;

public class CloudAccountWrapper implements Serializable{

    private int cloudAccountId;
    private String name;
    private String accountName;
    private String accessToken;
    private int cloudProvider;




    public int getCloudAccountId() {
        return cloudAccountId;
    }

    public void setCloudAccountId(int cloudAccountId) {
        this.cloudAccountId = cloudAccountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public int getCloudProvider() {
        return cloudProvider;
    }

    public void setCloudProvider(int cloudProvider) {
        this.cloudProvider = cloudProvider;
    }
}
