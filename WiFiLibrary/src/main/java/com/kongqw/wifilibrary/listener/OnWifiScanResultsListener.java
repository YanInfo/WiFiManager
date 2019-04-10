package com.kongqw.wifilibrary.listener;

import android.net.wifi.ScanResult;

import java.util.List;

/**
 * @Author: zhangyan
 * @Date: 2019/4/10 10:17
 * WIFI扫描结果的回调接口
 */
public interface OnWifiScanResultsListener {

    /**
     * 扫描结果的回调
     *
     * @param scanResults 扫描结果
     */
    void onScanResults(List<ScanResult> scanResults);
}
