package com.iths.redis.cache;

import java.io.Serializable;

/**
 * @author sen.huang
 * @date 2019/1/30.
 */
public class LockHold implements Serializable {
    private static final long serialVersionUID = -936701741712148230L;
    private String lockValue;

    private String releaseCode;

    public LockHold(String releaseCode, String lockValue) {
        this.lockValue = lockValue;
        this.releaseCode = releaseCode;
    }

    public String getReleaseCode() {
        return releaseCode;
    }

    public void setReleaseCode(String releaseCode) {
        this.releaseCode = releaseCode;
    }

    public String getLockValue() {
        return lockValue;
    }

    public void setLockValue(String lockValue) {
        this.lockValue = lockValue;
    }
}
