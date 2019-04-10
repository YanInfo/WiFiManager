package com.kongqw.wifilibrary.listener;

/**
 * @Author: zhangyan
 * @Date: 2019/4/10 10:19
 * WIFI打开关闭的回调接口
 */
public interface OnWifiEnabledListener {

    /**
     * WIFI开关的回调
     *
     * @param enabled true 可用 false 不可用
     */
    void onWifiEnabled(boolean enabled);
}
